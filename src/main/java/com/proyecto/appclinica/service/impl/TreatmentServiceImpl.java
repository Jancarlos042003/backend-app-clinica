package com.proyecto.appclinica.service.impl;

import ca.uhn.fhir.context.FhirContext;
import com.proyecto.appclinica.constant.MedicationCodingSystems;
import com.proyecto.appclinica.constant.TreatmentExtensionConstants;
import com.proyecto.appclinica.event.medication.MedicationRequestCreatedEvent;
import com.proyecto.appclinica.model.dto.treatment.CreateTreatmentDto;
import com.proyecto.appclinica.model.dto.treatment.TreatmentRecordDto;
import com.proyecto.appclinica.model.dto.treatment.TreatmentResultDto;
import com.proyecto.appclinica.repository.FhirMedicationRequestRepository;
import com.proyecto.appclinica.repository.FhirPatientRepository;
import com.proyecto.appclinica.repository.MedicationRepository;
import com.proyecto.appclinica.service.TreatmentService;
import com.proyecto.appclinica.util.PatientUtils;
import com.proyecto.appclinica.util.PeriodUnitMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TreatmentServiceImpl implements TreatmentService {
    private final FhirMedicationRequestRepository fhirMedicationRequestRepository;
    private final FhirPatientRepository fhirPatientRepository;
    private final MedicationRepository medicationRepository;
    private final ApplicationEventPublisher publisher;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Getter
    private final FhirContext fhirContext;

    public TreatmentRecordDto createTreatment(CreateTreatmentDto treatmentDTO) {
        MedicationRequest request = new MedicationRequest();

        String patientId = PatientUtils.getPatientIdForIdentifier(treatmentDTO.getDni(), fhirPatientRepository);

        // Referencia al paciente
        request.setSubject(new Reference("Patient/" + patientId));

        // Medicamento con codificación
        CodeableConcept medicationConcept = createMedicationCodeableConcept(treatmentDTO);
        request.setMedication(medicationConcept);

        // Dosis estructurada
        Dosage dosage = createDosage(treatmentDTO);

        // Crear componente de dispensación con periodo de validez
        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest =
                createDispenseRequest(treatmentDTO.getStartDate(), treatmentDTO.getDuration(), treatmentDTO.getDurationUnit());

        // Inicializar el contador de dosis tomadas a 0
        Extension dosesTakenExtension = new Extension(TreatmentExtensionConstants.DOSES_TAKEN_EXTENSION);
        dosesTakenExtension.setValue(new IntegerType(0));
        request.addExtension(dosesTakenExtension);

        // Configurar extensiones de horario (irregulares y personalizados)
        configureScheduleExtensions(request, treatmentDTO);

        request.setDispenseRequest(dispenseRequest);
        request.addDosageInstruction(dosage);
        request.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        request.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

        MedicationRequest resultRequest = fhirMedicationRequestRepository.saveMedicationRequest(request);

        // Publicamos el evento después de guardar el tratamiento
        publisher.publishEvent(new MedicationRequestCreatedEvent(resultRequest));

        return convertToTreatmentRecordDto(resultRequest);
    }

    public TreatmentRecordDto updateTreatment(String medicationRequestId, CreateTreatmentDto treatmentDTO) {
        // Eliminar todas las MedicationEntity existentes que hacen referencia a este MedicationRequest
        medicationRepository.deleteAll(medicationRepository.findByMedicationRequestId(medicationRequestId));

        MedicationRequest existingRequest = fhirMedicationRequestRepository.getMedicationRequestById(medicationRequestId);

        // Actualizar los campos necesarios si no son nulos
        if (treatmentDTO.getNameMedicine() != null) {
            CodeableConcept medicationConcept = createMedicationCodeableConcept(treatmentDTO);
            existingRequest.setMedication(medicationConcept);
        }

        // Limpiar las instrucciones de dosificación anteriores
        existingRequest.getDosageInstruction().clear();

        // Crear nueva dosificación si se proporcionan los datos necesarios
        if (treatmentDTO.getDoseValue() != null && treatmentDTO.getDoseUnit() != null) {
            Dosage dosage = createDosage(treatmentDTO);
            existingRequest.addDosageInstruction(dosage);
        }

        // Actualizar fechas si se proporciona la fecha de inicio
        if (treatmentDTO.getStartDate() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest =
                    createDispenseRequest(treatmentDTO.getStartDate(), treatmentDTO.getDuration(), treatmentDTO.getDurationUnit());
            existingRequest.setDispenseRequest(dispenseRequest);
        }

        // Limpiar extensiones existentes relacionadas con horarios
        existingRequest.getExtension().removeIf(ext ->
                TreatmentExtensionConstants.IRREGULAR_SCHEDULE_EXTENSION.equals(ext.getUrl()) ||
                        TreatmentExtensionConstants.SCHEDULE_PATTERN_EXTENSION.equals(ext.getUrl()) ||
                        TreatmentExtensionConstants.CUSTOM_TIME_EXTENSION.equals(ext.getUrl())
        );

        // Configurar extensiones de horario (irregulares y personalizados)
        configureScheduleExtensions(existingRequest, treatmentDTO);

        // Guardar la solicitud actualizada en el repositorio
        MedicationRequest updatedRequest = fhirMedicationRequestRepository.updateMedicationRequest(existingRequest);

        // Lanzar el mismo evento que se envía en createTreatment para recrear las entidades MedicationEntity
        publisher.publishEvent(new MedicationRequestCreatedEvent(updatedRequest));

        return convertToTreatmentRecordDto(updatedRequest);
    }

    public TreatmentResultDto cancelTreatment(String medicationRequestId) {
        MedicationRequest medicationRequest = fhirMedicationRequestRepository.getMedicationRequestById(medicationRequestId);

        // Cambiar el estado a CANCELLED
        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.CANCELLED);

        // Establecer la razón de cancelación
        medicationRequest.setStatusReason(new CodeableConcept().setText("Tratamiento cancelado por solicitud"));

        TreatmentResultDto resultDto = new TreatmentResultDto();

        try {
            fhirMedicationRequestRepository.updateMedicationRequest(medicationRequest);
            resultDto.setSuccess(true);
            resultDto.setMessage("Tratamiento cancelado exitosamente");
            resultDto.setTreatmentId(Long.valueOf(medicationRequestId));
        } catch (Exception e) {
            resultDto.setSuccess(false);
            resultDto.setMessage("Error al cancelar el tratamiento: " + e.getMessage());
        }

        return resultDto;
    }

    public TreatmentResultDto completeTreatment(String medicationRequestId) {
        MedicationRequest medicationRequest = fhirMedicationRequestRepository.getMedicationRequestById(medicationRequestId);

        // Cambiar el estado a COMPLETED
        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
        medicationRequest.setStatusReason(new CodeableConcept().setText("Tratamiento completado según lo prescrito"));

        TreatmentResultDto resultDto = new TreatmentResultDto();

        try {
            fhirMedicationRequestRepository.updateMedicationRequest(medicationRequest);
            resultDto.setSuccess(true);
            resultDto.setMessage("Tratamiento completado exitosamente");
            resultDto.setTreatmentId(Long.valueOf(medicationRequestId));
        } catch (Exception e) {
            resultDto.setSuccess(false);
            resultDto.setMessage("Error al completar el tratamiento: " + e.getMessage());
        }

        return resultDto;
    }

    public List<TreatmentRecordDto> getAllMedicationRequestsByPatientId(String patientId) {
        List<MedicationRequest> medicationRequests = fhirMedicationRequestRepository.findMedicationRequestsByPatientId(patientId);
        return medicationRequests.stream()
                .map(this::convertToTreatmentRecordDto)
                .toList();
    }

    public List<TreatmentRecordDto> getAllMedicationRequestsByPatientIdAndStatus(String patientId, String status) {
        List<MedicationRequest> medicationRequests = fhirMedicationRequestRepository.findMedicationRequestsByPatientIdAndStatus(patientId, status);
        return medicationRequests.stream()
                .map(this::convertToTreatmentRecordDto)
                .toList();
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, Integer duration, String durationUnit) {
        if (startDate == null) {
            return LocalDateTime.now().plusDays(7);
        }

        if (duration == null) {
            return startDate.plusDays(7);
        }

        if (durationUnit == null) {
            return startDate.plusDays(duration);
        }

        String normalized = Normalizer.normalize(durationUnit.toLowerCase().trim(),
                        Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        return switch (normalized) {
            case "hour", "h", "hora", "horas" -> startDate.plusHours(duration);
            case "day", "d", "dia", "dias" -> startDate.plusDays(duration);
            case "week", "w", "semana", "semanas" -> startDate.plusWeeks(duration);
            case "month", "m", "mes", "meses" -> startDate.plusMonths(duration);
            default -> startDate.plusDays(duration);
        };
    }

    /**
     * Genera un código de medicamento para usar en el campo code del coding.
     * En un caso real, esto consultaría una base de datos de medicamentos o un servicio externo.
     *
     * @param medicationName Nombre del medicamento
     * @param doseValue      Valor de la dosis
     * @param doseUnit       Unidad de la dosis
     * @return Código del medicamento
     */
    private String getMedicationCode(String medicationName, Double doseValue, String doseUnit) {
        // En un entorno real, este metodo consultaría una API como RxNorm para obtener
        // el código correcto. Para este ejemplo, usaremos códigos fijos para algunos medicamentos comunes
        // o generaremos un código basado en un hash del nombre del medicamento

        // Códigos de ejemplo para medicamentos comunes
        if ("Paracetamol".equalsIgnoreCase(medicationName) && doseValue != null && doseValue == 500.0 && "mg".equalsIgnoreCase(doseUnit)) {
            return "161"; // Código RxNorm para Paracetamol 500mg
        } else if ("Ibuprofeno".equalsIgnoreCase(medicationName) && doseValue != null && doseValue == 400.0 && "mg".equalsIgnoreCase(doseUnit)) {
            return "197806"; // Código RxNorm para Ibuprofeno 400mg
        } else if ("Amoxicilina".equalsIgnoreCase(medicationName) && doseValue != null && doseValue == 500.0 && "mg".equalsIgnoreCase(doseUnit)) {
            return "723"; // Código RxNorm para Amoxicilina 500mg
        } else if ("Aspirina".equalsIgnoreCase(medicationName) && doseValue != null && doseValue == 100.0 && "mg".equalsIgnoreCase(doseUnit)) {
            return "1191"; // Código RxNorm para Aspirina 100mg
        } else if ("Omeprazol".equalsIgnoreCase(medicationName) && doseValue != null && doseValue == 20.0 && "mg".equalsIgnoreCase(doseUnit)) {
            return "198073"; // Código RxNorm para Omeprazol 20mg
        }

        // Para otros medicamentos, generamos un código basado en un hash simple
        // Esto es solo para demostración. En producción se recomendaría usar un servicio real de RxNorm.
        String medicationKey = medicationName.toLowerCase() + "_" + doseValue + "_" + doseUnit.toLowerCase();
        return String.valueOf(Math.abs(medicationKey.hashCode()) % 1000000);
    }

    /**
     * Crea un CodeableConcept para el medicamento con su información de codificación
     */
    private CodeableConcept createMedicationCodeableConcept(CreateTreatmentDto treatmentDTO) {
        CodeableConcept medicationConcept = new CodeableConcept()
                .setText(treatmentDTO.getNameMedicine());

        // Agregar la información de coding
        Coding medicationCoding = new Coding()
                .setSystem(MedicationCodingSystems.RXNORM)
                .setCode(getMedicationCode(treatmentDTO.getNameMedicine(), treatmentDTO.getDoseValue(), treatmentDTO.getDoseUnit()))
                .setDisplay(treatmentDTO.getNameMedicine() + " " + treatmentDTO.getDoseValue() + treatmentDTO.getDoseUnit() + " tablet");

        medicationConcept.addCoding(medicationCoding);
        return medicationConcept;
    }

    /**
     * Convierte un MedicationRequest de FHIR a un TreatmentRecordDto
     */
    public TreatmentRecordDto convertToTreatmentRecordDto(MedicationRequest request) {
        if (request == null) {
            return null;
        }

        TreatmentRecordDto dto = new TreatmentRecordDto();

        // Establecer ID
        dto.setId(Long.valueOf(request.getIdElement().getIdPart())); // Convertir el ID de FHIR a Long

        // Obtener el nombre del medicamento
        if (request.hasMedication() && request.getMedicationCodeableConcept() != null) {
            dto.setNameMedicine(request.getMedicationCodeableConcept().getText());
        }

        // Obtener dosis y frecuencia
        if (!request.getDosageInstruction().isEmpty()) {
            var dosage = request.getDosageInstructionFirstRep();

            // Dosis
            if (dosage.hasDoseAndRate() && dosage.getDoseAndRateFirstRep().hasDose()) {
                Quantity dose = (Quantity) dosage.getDoseAndRateFirstRep().getDose();
                dto.setDose(dose.getValue() + " " + dose.getUnit());
            }

            // Frecuencia
            if (dosage.hasTiming() && dosage.getTiming().hasRepeat()) {
                var repeat = dosage.getTiming().getRepeat();
                StringBuilder frequencyText = new StringBuilder("cada ");

                if (repeat.hasFrequency()) {
                    if (repeat.hasPeriod() && repeat.hasPeriodUnit()) {
                        if (repeat.getFrequency() == 1) {
                            frequencyText.append(repeat.getPeriod()).append(" ");
                            // Usar singular o plural según el valor del periodo
                            frequencyText.append(formatPeriodUnitWithNumber(repeat.getPeriodUnit(), repeat.getPeriod().doubleValue()));
                        } else {
                            frequencyText.append(repeat.getFrequency())
                                    .append(repeat.getFrequency() == 1 ? " vez por " : " veces por ")
                                    .append(formatPeriodUnitInPlural(repeat.getPeriodUnit()));
                        }
                    } else {
                        frequencyText.append(repeat.getFrequency())
                                .append(repeat.getFrequency() == 1 ? " vez al día" : " veces al día");
                    }
                    dto.setFrequency(frequencyText.toString());
                }
            }
        }

        // Fechas y duración
        if (request.hasDispenseRequest() && request.getDispenseRequest().hasValidityPeriod()) {
            var period = request.getDispenseRequest().getValidityPeriod();

            if (period.hasStart()) {
                LocalDateTime startDate = period.getStart().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                dto.setStartDate(startDate.format(DATE_FORMATTER));
            }

            if (period.hasEnd()) {
                LocalDateTime endDate = period.getEnd().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                dto.setEndDate(endDate.format(DATE_FORMATTER));

                // Calcular duración si hay fecha de inicio y fin
                if (period.hasStart()) {
                    long days = java.time.Duration.between(
                            period.getStart().toInstant(),
                            period.getEnd().toInstant()
                    ).toDays();
                    // Usar singular o plural según el número de días
                    dto.setDuration(days + (days == 1 ? " día" : " días"));
                }
            }
        }

        // Estado
        if (request.hasStatus()) {
            dto.setStatus(formatStatus(request.getStatus()));
        }

        // Establecer progreso
        dto.setProgress(calculateProgress(request));

        return dto;
    }

    private String formatStatus(MedicationRequest.MedicationRequestStatus status) {
        if (status == null) return "desconocido";

        return switch (status) {
            case ACTIVE -> "activo";
            case COMPLETED -> "completado";
            case CANCELLED -> "cancelado";
            case ONHOLD -> "en pausa";
            case STOPPED -> "detenido";
            default -> status.toString().toLowerCase();
        };
    }

    private String formatPeriodUnit(Timing.UnitsOfTime unit) {
        if (unit == null) return "horas";

        return switch (unit) {
            case H -> "horas";
            case D -> "días";
            case WK -> "semanas";
            case MO -> "meses";
            default -> unit.toString().toLowerCase();
        };
    }

    private Integer calculateProgress(MedicationRequest request) {
        // Si el tratamiento está completado o cancelado
        if (request.getStatus() == MedicationRequest.MedicationRequestStatus.COMPLETED) {
            return 100;
        } else if (request.getStatus() == MedicationRequest.MedicationRequestStatus.CANCELLED) {
            return 0;
        }

        // Calcular progreso basado en la fecha actual y el periodo de validez
        if (request.hasDispenseRequest() && request.getDispenseRequest().hasValidityPeriod()) {
            var period = request.getDispenseRequest().getValidityPeriod();
            if (period.hasStart() && period.hasEnd()) {
                var now = new java.util.Date();
                var start = period.getStart();
                var end = period.getEnd();

                // Si ya terminó pero no está marcado como completado
                if (now.after(end)) {
                    return 100;
                }

                // Si aún no ha comenzado
                if (now.before(start)) {
                    return 0;
                }

                // Calcular progreso
                long totalDuration = end.getTime() - start.getTime();
                long elapsed = now.getTime() - start.getTime();

                if (totalDuration > 0) {
                    return (int) ((elapsed * 100) / totalDuration);
                }
            }
        }

        return 0; // Valor por defecto
    }

    /**
     * Configura las extensiones relacionadas con los horarios en una solicitud de medicación
     *
     * @param request      La solicitud de medicación a configurar
     * @param treatmentDTO DTO con la información del tratamiento
     */
    private void configureScheduleExtensions(MedicationRequest request, CreateTreatmentDto treatmentDTO) {
        // Manejar horarios irregulares si están especificados
        if (Boolean.TRUE.equals(treatmentDTO.getIsIrregular()) &&
                treatmentDTO.getSchedulePatterns() != null &&
                !treatmentDTO.getSchedulePatterns().isEmpty()) {

            // Añadir extensión para indicar que es un horario irregular
            Extension irregularExt = new Extension(TreatmentExtensionConstants.IRREGULAR_SCHEDULE_EXTENSION);
            irregularExt.setValue(new BooleanType(true));
            request.addExtension(irregularExt);

            // Añadir extensión para patrones de horario (MORNING, NOON, AFTERNOON, EVENING, NIGHT)
            String patternString = String.join(",", treatmentDTO.getSchedulePatterns());
            Extension patternExt = new Extension(TreatmentExtensionConstants.SCHEDULE_PATTERN_EXTENSION);
            patternExt.setValue(new StringType(patternString));
            request.addExtension(patternExt);
        }
        // Manejar horarios personalizados si están especificados
        else if (Boolean.TRUE.equals(treatmentDTO.getHasCustomTimes()) &&
                treatmentDTO.getCustomTimes() != null &&
                !treatmentDTO.getCustomTimes().isEmpty()) {

            String customTimesString = String.join(",", treatmentDTO.getCustomTimes());
            Extension customTimesExt = new Extension(TreatmentExtensionConstants.CUSTOM_TIME_EXTENSION);
            customTimesExt.setValue(new StringType(customTimesString));
            request.addExtension(customTimesExt);
        }
    }

    /**
     * Crea un objeto Dosage con la información de dosificación y frecuencia
     *
     * @param treatmentDTO DTO con la información del tratamiento
     * @return Objeto Dosage configurado
     */
    private Dosage createDosage(CreateTreatmentDto treatmentDTO) {
        Dosage dosage = new Dosage();

        // Configurar dosis
        dosage.addDoseAndRate()
                .setDose(new Quantity()
                        .setValue(treatmentDTO.getDoseValue())
                        .setUnit(treatmentDTO.getDoseUnit())
                        .setSystem("http://unitsofmeasure.org") // UCUM
                );

        // Configurar timing (frecuencia) si se proporcionan los datos necesarios
        if (treatmentDTO.getFrequency() != null) {
            Timing timing = new Timing();
            timing.setRepeat(new Timing.TimingRepeatComponent()
                    .setFrequency(treatmentDTO.getFrequency())
                    .setPeriod(treatmentDTO.getPeriod() != null ? treatmentDTO.getPeriod() : 1)
                    .setPeriodUnit(mapPeriodUnit(treatmentDTO.getPeriodUnit()))
            );
            dosage.setTiming(timing);
        }

        return dosage;
    }

    /**
     * Configura el período de validez para una solicitud de medicamento
     *
     * @param startDateTime Fecha de inicio del tratamiento
     * @param duration      Duración del tratamiento
     * @param durationUnit  Unidad de la duración (day, week, etc)
     * @return Componente de solicitud de dispensación configurado con el período de validez
     */
    private MedicationRequest.MedicationRequestDispenseRequestComponent createDispenseRequest(
            LocalDateTime startDateTime, Integer duration, String durationUnit) {

        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // Usar valores por defecto si son nulos
        Integer effectiveDuration = duration != null ? duration : 7;
        String effectiveDurationUnit = durationUnit != null ? durationUnit : "day";

        // Calcular la fecha de finalización basada en la duración y su unidad
        LocalDateTime endDateTime = calculateEndDate(startDateTime, effectiveDuration, effectiveDurationUnit);
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        return new MedicationRequest.MedicationRequestDispenseRequestComponent()
                .setValidityPeriod(new Period()
                        .setStart(startDate)
                        .setEnd(endDate)
                );
    }

    private String formatPeriodUnitWithNumber(Timing.UnitsOfTime unit, double number) {
        if (unit == null) return number == 1 ? "hora" : "horas";

        return switch (unit) {
            case H -> number == 1 ? "hora" : "horas";
            case D -> number == 1 ? "día" : "días";
            case WK -> number == 1 ? "semana" : "semanas";
            case MO -> number == 1 ? "mes" : "meses";
            default -> unit.toString().toLowerCase();
        };
    }

    private String formatPeriodUnitInPlural(Timing.UnitsOfTime unit) {
        if (unit == null) return "día";

        return switch (unit) {
            case H -> "hora";
            case D -> "día";
            case WK -> "semana";
            case MO -> "mes";
            default -> unit.toString().toLowerCase() + "s"; // Agregar 's' como fallback para pluralizar
        };
    }

    private Timing.UnitsOfTime mapPeriodUnit(String periodUnit) {
        if (periodUnit == null) return Timing.UnitsOfTime.H; // Horas por defecto

        return switch (periodUnit.toLowerCase()) {
            case "hour", "h", "hora", "horas" -> Timing.UnitsOfTime.H;
            case "day", "d", "dia", "días", "dias" -> Timing.UnitsOfTime.D;
            case "week", "w", "semana", "semanas" -> Timing.UnitsOfTime.WK;
            case "month", "m", "mes", "meses" -> Timing.UnitsOfTime.MO;
            default -> Timing.UnitsOfTime.H;
        };
    }
}
