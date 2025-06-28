package com.proyecto.appclinica.event.medication;

import com.proyecto.appclinica.constant.FhirConstants;
import com.proyecto.appclinica.model.entity.EDayTimePattern;
import com.proyecto.appclinica.model.entity.EMedicationStatementStatus;
import com.proyecto.appclinica.model.entity.MedicationEntity;
import com.proyecto.appclinica.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@EnableAsync
@RequiredArgsConstructor
public class MedicationRequestListener {

    private final MedicationRepository medicationRepository;
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})");

    @EventListener
    @Async
    public void calculateMedicineSupply(MedicationRequestCreatedEvent event) {
        MedicationRequest medicationRequest = event.getMedicationRequest();

        // Comprobar si es un horario irregular
        boolean isIrregular = hasIrregularSchedule(medicationRequest);

        if (isIrregular) {
            createMedicationsWithIrregularSchedule(medicationRequest);
        } else {
            // Comprobar si tiene horarios personalizados
            if (hasCustomTimeSchedule(medicationRequest)) {
                createMedicationsWithCustomTimeSchedule(medicationRequest);
            } else {
                // Comportamiento regular existente
                List<Long> schedule = calculateSchedule(medicationRequest);

                schedule.forEach(s -> {
                    MedicationEntity medication = createMedication(medicationRequest);
                    medication.setTimeOfTaking(Timestamp.from(Instant.ofEpochSecond(s)));
                    medicationRepository.save(medication);
                });
            }
        }
    }

    private boolean hasCustomTimeSchedule(MedicationRequest request) {
        return request.getExtension().stream()
                .anyMatch(ext -> FhirConstants.CUSTOM_TIME_EXTENSION.equals(ext.getUrl()));
    }

    private List<LocalTime> getCustomTimes(MedicationRequest request) {
        return request.getExtension().stream()
                .filter(ext -> FhirConstants.CUSTOM_TIME_EXTENSION.equals(ext.getUrl()))
                .map(ext -> ((StringType) ext.getValue()).getValue())
                .flatMap(timeStr -> Arrays.stream(timeStr.split(",")))
                .map(String::trim)
                .map(this::parseTime)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<LocalTime> parseTime(String timeStr) {
        try {
            // Intenta parsear formato HH:mm
            Matcher matcher = TIME_PATTERN.matcher(timeStr);
            if (matcher.matches()) {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = Integer.parseInt(matcher.group(2));

                // Validar que la hora sea válida
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return Optional.of(LocalTime.of(hour, minute));
                }
            }

            // Intenta parsear como formato LocalTime estándar
            return Optional.of(LocalTime.parse(timeStr));
        } catch (DateTimeParseException e) {
            // Si no se puede parsear, intenta buscar en EDayTimePattern
            try {
                EDayTimePattern pattern = EDayTimePattern.valueOf(timeStr.toUpperCase());
                return Optional.of(pattern.getDefaultTime());
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
        }
    }

    private void createMedicationsWithCustomTimeSchedule(MedicationRequest request) {
        List<LocalTime> customTimes = getCustomTimes(request);
        if (customTimes.isEmpty()) {
            return;
        }

        // Obtener fechas de inicio y fin
        Instant startInstant = request.getDispenseRequest().getValidityPeriod().getStart().toInstant();
        Instant endInstant = request.getDispenseRequest().getValidityPeriod().getEnd().toInstant();

        LocalDate startDate = startInstant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endInstant.atZone(ZoneId.systemDefault()).toLocalDate();

        // Para cada día dentro del rango, crear medicaciones según los horarios personalizados
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (LocalTime time : customTimes) {
                LocalDateTime dateTime = LocalDateTime.of(currentDate, time);
                ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
                Instant medicationTime = zonedDateTime.toInstant();

                MedicationEntity medication = createMedication(request);
                medication.setTimeOfTaking(Timestamp.from(medicationTime));
                medication.setSchedulePattern(time.format(DateTimeFormatter.ofPattern("HH:mm")));
                medicationRepository.save(medication);
            }

            currentDate = currentDate.plusDays(1);
        }
    }

    private boolean hasIrregularSchedule(MedicationRequest request) {
        Optional<Extension> irregularExt = request.getExtension().stream()
                .filter(ext -> FhirConstants.IRREGULAR_SCHEDULE_EXTENSION.equals(ext.getUrl()))
                .findFirst();

        return irregularExt.isPresent() &&
               irregularExt.get().getValue() instanceof BooleanType &&
               ((org.hl7.fhir.r4.model.BooleanType) irregularExt.get().getValue()).booleanValue();
    }

    private String getSchedulePattern(MedicationRequest request) {
        return request.getExtension().stream()
                .filter(ext -> FhirConstants.SCHEDULE_PATTERN_EXTENSION.equals(ext.getUrl()))
                .findFirst()
                .map(ext -> ((StringType) ext.getValue()).getValue())
                .orElse("");
    }

    private void createMedicationsWithIrregularSchedule(MedicationRequest request) {
        String patternStr = getSchedulePattern(request);
        if (patternStr.isEmpty()) {
            return;
        }

        // Obtener fechas de inicio y fin
        Instant startInstant = request.getDispenseRequest().getValidityPeriod().getStart().toInstant();
        Instant endInstant = request.getDispenseRequest().getValidityPeriod().getEnd().toInstant();

        LocalDate startDate = startInstant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endInstant.atZone(ZoneId.systemDefault()).toLocalDate();

        // Convertir los patrones de texto a EDayTimePattern
        List<EDayTimePattern> patterns = Arrays.stream(patternStr.split(","))
                .map(String::trim)
                .map(EDayTimePattern::valueOf)
                .toList();

        // Para cada día dentro del rango, crear medicaciones según los patrones
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            for (EDayTimePattern pattern : patterns) {
                LocalDateTime dateTime = LocalDateTime.of(currentDate, pattern.getDefaultTime());
                ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
                Instant medicationTime = zonedDateTime.toInstant();

                MedicationEntity medication = createMedication(request);
                medication.setTimeOfTaking(Timestamp.from(medicationTime));
                medication.setIrregular(true);
                medication.setSchedulePattern(pattern.name());
                medicationRepository.save(medication);
            }

            currentDate = currentDate.plusDays(1);
        }
    }

    private MedicationEntity createMedication(MedicationRequest request) {
        MedicationEntity medication = new MedicationEntity();

        // Obtener la información de la dosis
        Dosage dosage = request.getDosageInstructionFirstRep();
        Quantity quantity = dosage.getDoseAndRateFirstRep().getDoseQuantity();

        // Obtener los valores de la dosis
        BigDecimal quantityValue = quantity.getValue();
        String quantityUnit = quantity.getUnit();

        medication.setNameMedicine(request.getMedicationCodeableConcept().getText());
        medication.setDoseValue(quantityValue);
        medication.setDoseUnit(quantityUnit);
        medication.setPatientId(request.getSubject().getReference().replace("Patient/", ""));
        medication.setMedicationRequestId(request.getIdElement().getIdPart());

        log.info("Creating medication for request ID: {}", medication.getMedicationRequestId());

        medication.setStatus(EMedicationStatementStatus.INTENDED);
        medication.setIrregular(hasIrregularSchedule(request));

        if (medication.isIrregular()) {
            medication.setSchedulePattern(getSchedulePattern(request));
        }

        return medication;
    }

    private List<Long> calculateSchedule(MedicationRequest request) {
        List<Long> schedule = new ArrayList<>();

        // Obtener la fecha de inicio y fin de la validez del medicamento
        Instant dateStart = request.getDispenseRequest().getValidityPeriod().getStart().toInstant();
        Instant dateEnd = request.getDispenseRequest().getValidityPeriod().getEnd().toInstant();

        // Convertir a LocalDateTime para facilitar cálculos de fechas
        LocalDateTime startDateTime = LocalDateTime.ofInstant(dateStart, ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(dateEnd, ZoneId.systemDefault());

        Dosage dosage = request.getDosageInstructionFirstRep();

        // Obtener la frecuencia de la dosis
        int frequency = dosage.getTiming().getRepeat().getFrequency();
        BigDecimal period = dosage.getTiming().getRepeat().getPeriod();
        String periodUnit = dosage.getTiming().getRepeat().getPeriodUnit().toCode();

        // Calcular el intervalo entre dosis basado en la unidad de periodo
        long intervalSeconds = calculateIntervalInSeconds(periodUnit, period.intValue(), frequency, startDateTime);

        // Calcular el número total de dosis basado en la duración del tratamiento
        long totalDurationSeconds = ChronoUnit.SECONDS.between(startDateTime, endDateTime);
        long totalDoses = totalDurationSeconds / intervalSeconds;

        // Generar el horario de las dosis
        for (int i = 0; i < totalDoses; i++) {
            LocalDateTime doseDateTime = startDateTime.plus(intervalSeconds * i, ChronoUnit.SECONDS);
            Instant doseInstant = doseDateTime.atZone(ZoneId.systemDefault()).toInstant();
            schedule.add(doseInstant.getEpochSecond());
        }

        return schedule;
    }

    private long calculateIntervalInSeconds(String periodUnit, int periodValue, int frequency, LocalDateTime startDate) {
        // Calcula el intervalo en segundos entre cada dosis
        long secondsInPeriod;

        switch (periodUnit) {
            case "h":
                secondsInPeriod = 3600L * periodValue;// horas a segundos
                break;
            case "d":
                secondsInPeriod = 86400L * periodValue; // días a segundos
                break;
            case "wk":
                secondsInPeriod = 604800L * periodValue; // semanas a segundos
                break;
            case "mo":
                // Cálculo preciso para meses, considerando la duración real del mes actual
                YearMonth yearMonth = YearMonth.from(startDate);
                int daysInMonth = yearMonth.lengthOfMonth();
                secondsInPeriod = 86400L * daysInMonth * periodValue; // días del mes actual a segundos
                break;
            default:
                throw new IllegalArgumentException("Unidad de periodo no soportada: " + periodUnit);
        }

        // Dividir por la frecuencia para obtener el intervalo entre dosis
        return secondsInPeriod / frequency;
    }
}
