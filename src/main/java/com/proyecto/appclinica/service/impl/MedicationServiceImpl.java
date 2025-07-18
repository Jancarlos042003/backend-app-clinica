package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.event.medication.MedicationCompletedEvent;
import com.proyecto.appclinica.event.medication.MedicationReminderEvent;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.exception.StatusException;
import com.proyecto.appclinica.model.dto.treatment.medication.MedicationResponseDto;
import com.proyecto.appclinica.model.dto.treatment.medication.MedicationStatusUpdateDto;
import com.proyecto.appclinica.model.entity.EMedicationStatementStatus;
import com.proyecto.appclinica.model.entity.MedicationEntity;
import com.proyecto.appclinica.model.entity.MedicationSettings;
import com.proyecto.appclinica.model.entity.UserSettings;
import com.proyecto.appclinica.repository.MedicationRepository;
import com.proyecto.appclinica.repository.UserSettingsRepository;
import com.proyecto.appclinica.service.MedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Mapa para llevar conteo de recordatorios enviados por medicamento
    private final Map<String, Integer> reminderAttempts = new HashMap<>();

    @Override
    public List<MedicationResponseDto> getMedicationsToday(String patientId) {
        LocalDate today = LocalDate.now();
        return getMedicationsByDate(patientId, today);
    }

    @Override
    public List<MedicationResponseDto> getMedicationsByDate(String patientId, LocalDate date) {
        // Calculamos el inicio y fin del día
        LocalDateTime startOfDay = date.atStartOfDay(); // Inicio del día a las 00:00
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // Fin del día a las 23:59:59.9999999999

        // Convertimos a timestamp para la consulta
        Timestamp startTimestamp = Timestamp.valueOf(startOfDay);
        Timestamp endTimestamp = Timestamp.valueOf(endOfDay);

        // Buscamos medicamentos del paciente para este rango de tiempo
        List<MedicationEntity> medications = medicationRepository.findAllByPatientIdAndTimeOfTakingBetween(
                patientId, startTimestamp, endTimestamp);

        // Ordenamos los medicamentos por hora de toma antes de convertir a DTOs
        medications.sort(Comparator.comparing(MedicationEntity::getTimeOfTaking));

        // Convertimos las entidades a DTOs
        return medications.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public List<MedicationResponseDto> getMedicationsInDateRange(String patientId, LocalDate startDate, LocalDate endDate) {
        // Calculamos el inicio y fin de los días respectivos
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Convertimos a timestamp para la consulta
        Timestamp startTimestamp = Timestamp.valueOf(startDateTime);
        Timestamp endTimestamp = Timestamp.valueOf(endDateTime);

        // Buscamos medicamentos del paciente para este rango de fechas
        List<MedicationEntity> medications = medicationRepository.findAllByPatientIdAndTimeOfTakingBetween(
                patientId, startTimestamp, endTimestamp);

        // Ordenamos los medicamentos por hora de toma antes de convertir a DTOs
        medications.sort(Comparator.comparing(MedicationEntity::getTimeOfTaking));

        // Convertimos las entidades a DTOs
        return medications.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public List<MedicationResponseDto> getMedicationsByDateRangeAndStatus(String patientId, LocalDate startDate, LocalDate endDate, String status) {
        // Calculamos el inicio y fin de los días respectivos
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Convertimos a timestamp para la consulta
        Timestamp startTimestamp = Timestamp.valueOf(startDateTime);
        Timestamp endTimestamp = Timestamp.valueOf(endDateTime);

        List<MedicationEntity> medications;

        // Convertimos el string de estado a enumeración
        try {
            EMedicationStatementStatus medicationStatus = EMedicationStatementStatus.valueOf(status);

            // Buscamos medicamentos del paciente para este rango de fechas y con el estado específico
            medications = medicationRepository.findAllByPatientIdAndTimeOfTakingBetweenAndStatus(
                    patientId, startTimestamp, endTimestamp, medicationStatus);
        } catch (IllegalArgumentException e) {
            throw new StatusException("Estado de medicamento no válido: " + status);
        }

        // Ordenamos los medicamentos por hora de toma antes de convertir a DTOs
        medications.sort(Comparator.comparing(MedicationEntity::getTimeOfTaking));

        // Convertimos las entidades a DTOs
        return medications.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public MedicationResponseDto updateMedicationStatus(MedicationStatusUpdateDto updateDto) {
        // Buscamos el medicamento por su ID
        MedicationEntity medication = medicationRepository.findById(updateDto.medicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado con ID: " + updateDto.medicationId()));

        // Actualizamos el estado
        try {
            EMedicationStatementStatus newStatus = EMedicationStatementStatus.valueOf(updateDto.status());
            medication.setStatus(newStatus);

            // Si el estado es COMPLETED, publicamos el evento y reseteamos conteo de recordatorios
            if (newStatus == EMedicationStatementStatus.COMPLETED) {
                eventPublisher.publishEvent(new MedicationCompletedEvent(medication, medication.getPatientId()));
                reminderAttempts.remove(medication.getMedicationRequestId());
            }
        } catch (IllegalArgumentException e) {
            throw new StatusException("Estado de medicamento no válido: " + updateDto.status());
        }

        // Guardamos los cambios
        MedicationEntity updatedMedication = medicationRepository.save(medication);

        // Devolvemos el DTO actualizado
        return mapToResponseDto(updatedMedication);
    }

    @Override
    public void checkPendingMedications() {
        // Definimos explícitamente la zona horaria que usaremos para todas las operaciones
        ZoneId zoneId = ZoneId.of("America/Lima");

        // Obtenemos la fecha y hora actual en la zona horaria definida
        ZonedDateTime nowZoned = ZonedDateTime.now(zoneId);
        LocalDateTime now = nowZoned.toLocalDateTime();

        // Obtenemos medicamentos activos de hoy hasta ahora
        LocalDate today = LocalDate.now(zoneId);
        LocalDateTime startOfDay = today.atStartOfDay();
        Timestamp startTimestamp = Timestamp.valueOf(startOfDay);
        Timestamp nowTimestamp = Timestamp.valueOf(now);

        // Buscamos medicamentos con estado INTENDED cuya hora programada ya haya pasado
        List<MedicationEntity> activeMedications = medicationRepository.findAllByStatusAndTimeOfTakingBetween(
                EMedicationStatementStatus.INTENDED, startTimestamp, nowTimestamp);

        for (MedicationEntity medication : activeMedications) {
            String patientId = medication.getPatientId();

            // Convertimos el timestamp a LocalDateTime y luego a ZonedDateTime en nuestra zona horaria
            LocalDateTime scheduledLocalTime = medication.getTimeOfTaking().toLocalDateTime();
            ZonedDateTime scheduledTime = scheduledLocalTime.atZone(zoneId);

            // Obtenemos la configuración de medicamentos del usuario
            Optional<UserSettings> userSettingsOpt = userSettingsRepository.findByPatientId(patientId);

            // Si no hay configuración, usamos valores por defecto
            MedicationSettings medicationSettings = userSettingsOpt
                    .map(UserSettings::getMedicationSettings)
                    .orElse(MedicationSettings.builder().build());

            int toleranceWindowMinutes = medicationSettings.getToleranceWindowMinutes();

            // Calculamos los minutos desde la hora programada usando ZonedDateTime para asegurar
            // que la comparación sea correcta considerando la zona horaria
            long minutesElapsed = ChronoUnit.MINUTES.between(scheduledTime, nowZoned);

            // Solo marcamos como NOT_TAKEN si ha pasado la ventana de tolerancia y está en estado INTENDED
            if (minutesElapsed > toleranceWindowMinutes) {
                // Actualizamos el estado a NOT_TAKEN
                medication.setStatus(EMedicationStatementStatus.NOT_TAKEN);
                medicationRepository.save(medication);

                // Log para depuración
                log.info("Medicamento {} marcado como NOT_TAKEN. Tiempo programado: {}, minutos transcurridos: {}, ventana de tolerancia: {}",
                        medication.getId(), scheduledTime, minutesElapsed, toleranceWindowMinutes);
            } else {
                log.debug("Medicamento {} aún dentro de la ventana de tolerancia. Minutos transcurridos: {}, ventana: {}",
                        medication.getId(), minutesElapsed, toleranceWindowMinutes);
            }
        }
    }

    /**
     * Envía un recordatorio al paciente sobre un medicamento pendiente
     */
    private void sendMedicationReminder(MedicationEntity medication, int attemptNumber) {
        // Publicamos evento de recordatorio de medicamento
        eventPublisher.publishEvent(new MedicationReminderEvent(
                medication,
                medication.getPatientId(),
                attemptNumber
        ));
    }

    private MedicationResponseDto mapToResponseDto(MedicationEntity entity) {
        // Formateamos la dosis como "valor unidad" (ej: "10 mg")
        String formattedDose = entity.getDoseValue() + " " + entity.getDoseUnit();

        // Formateamos la hora como "HH:mm"
        String formattedTime = entity.getTimeOfTaking().toLocalDateTime().format(timeFormatter);

        // Formateamos la fecha como "yyyy-MM-dd" (formato ISO)
        String formattedDate = entity.getDate().toString();

        return new MedicationResponseDto(
                entity.getId(),
                entity.getNameMedicine(),
                formattedDose,
                formattedTime,
                formattedDate,
                entity.getStatus().getDescription()
        );
    }
}
