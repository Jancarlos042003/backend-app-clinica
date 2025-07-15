package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.event.sos.AlertSosCreateEvent;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.SosRequest;
import com.proyecto.appclinica.model.dto.SosResponse;
import com.proyecto.appclinica.model.dto.SosUpdateRequest;
import com.proyecto.appclinica.model.entity.ESosStatus;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.model.entity.SosEntity;
import com.proyecto.appclinica.model.entity.UserSettings;
import com.proyecto.appclinica.repository.PatientRepository;
import com.proyecto.appclinica.repository.SosRepository;
import com.proyecto.appclinica.repository.UserSettingsRepository;
import com.proyecto.appclinica.service.SosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SosServiceImpl implements SosService {

    private final PatientRepository patientRepository;
    private final ChatClient chatClient;
    private final SosRepository sosRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SosServiceImpl(PatientRepository patientRepository,
                          // Uso de @Lazy para evitar problemas de dependencia circular al diferir
                          // la inicialización del ChatClient hasta que realmente se necesite
                          // y @Qualifier para especificar el cliente de chat correcto
                          @Lazy @Qualifier("sosChatClient") ChatClient chatClient,
                          SosRepository sosRepository,
                          UserSettingsRepository userSettingsRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.patientRepository = patientRepository;
        this.chatClient = chatClient;
        this.sosRepository = sosRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SosResponse createSos(SosRequest sosRequest) {
        PatientEntity patient = patientRepository.findByPatientId(sosRequest.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "ID", sosRequest.getPatientId()));

        String aiReport = createAiReport(patient.getPatientId(), patient.getDni());
        log.info("Informe AI generado: {}", aiReport);

        SosEntity newSos = SosEntity.builder()
                .patientId(patient.getPatientId())
                .latitude(sosRequest.getLatitude())
                .longitude(sosRequest.getLongitude())
                .address(sosRequest.getAddress())
                .dateTime(LocalDateTime.now()) // Fecha y hora actual por defecto
                .patientMessage(sosRequest.getPatientMessage())
                .aiReport(aiReport)
                .status(ESosStatus.PENDING) // Estado inicial por defecto
                .build();

        SosEntity savedSos = sosRepository.save(newSos);

        UserSettings userSettings = userSettingsRepository.findByPatientId(patient.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de usuario no encontrada para el paciente con ID: " + patient.getPatientId()));

        // ⚠️ Forzar la carga de emergencyContacts mientras aún estás en la sesión activa
        userSettings.getEmergencyContacts().size(); // O usar Hibernate.initialize(userSettings.getEmergencyContacts())

        // Publicar evento para notificar a los contactos de emergencia
        eventPublisher.publishEvent(new AlertSosCreateEvent(patient, savedSos, userSettings));

        return buildSosResponse(savedSos);
    }

    @Override
    public SosResponse getSosById(Long id) {
        SosEntity sos = sosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sos", "ID", id.toString()));

        return buildSosResponse(sos);
    }

    @Override
    public SosResponse updateSos(Long id, SosUpdateRequest sosRequest) {
        SosEntity sos = sosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sos", "ID", id.toString()));

        if (sosRequest.getStatus() == ESosStatus.IN_PROGRESS) {
            sos.setStatus(ESosStatus.IN_PROGRESS);
        }
        if (sosRequest.getStatus() == ESosStatus.RESOLVED) {
            sos.setStatus(ESosStatus.RESOLVED);
        }

        SosEntity updatedSos = sosRepository.save(sos);

        return buildSosResponse(updatedSos);
    }

    @Override
    public String deleteSos(Long id) {
        SosEntity sos = sosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro SOS", "ID", id.toString()));

        sosRepository.deleteById(sos.getId());
        return "Registro SOS con ID " + id + " eliminado correctamente.";
    }

    @Override
    public List<SosResponse> getSosByPatientId(String patientId) {
        PatientEntity patient = patientRepository.findById(Long.valueOf(patientId))
                .orElseThrow(() -> new ResourceNotFoundException("Registro SOS", "ID del paciente", patientId));

        List<SosEntity> sosEntities = sosRepository.findByPatientId(patient.getPatientId());

        return sosEntities.stream()
                .map(this::buildSosResponse)
                .toList();
    }

    @Override
    public List<SosResponse> getAllSos() {
        List<SosEntity> sosEntities = sosRepository.findAll();

        return sosEntities.stream()
                .map(this::buildSosResponse)
                .toList();
    }

    @Override
    public List<SosResponse> getSosByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<SosEntity> sosEntities = sosRepository.findByDateTimeBetween(startDate, endDate);

        return sosEntities.stream()
                .map(this::buildSosResponse)
                .toList();
    }

    @Override
    public List<SosResponse> getSosByStatus(ESosStatus status) {
        List<SosEntity> sosEntities = sosRepository.findByStatus(status);

        return sosEntities.stream()
                .map(this::buildSosResponse)
                .toList();
    }

    private String createAiReport(String patientId, String identifier) {
        log.info("Generando informe AI para el paciente con ID: {}", patientId);


        return chatClient.prompt()
                .user("Genera un informe médico de emergencia completo. El identifier es " + identifier +
                        " que corresponde al DNI del paciente y el patientId " + patientId + " es el ID único del paciente en el sistema" +
                        ". Sigue el protocolo establecido comenzando por los registros SOS y utiliza todas las herramientas disponibles.")
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION,
                        String.format("category == 'patient_history' AND patient_id == '%s'", patientId)))
                .call()
                .content();
    }

    private SosResponse buildSosResponse(SosEntity sos) {
        return SosResponse.builder()
                .id(sos.getId())
                .patientId(sos.getPatientId())
                .latitude(sos.getLatitude().doubleValue())
                .longitude(sos.getLongitude().doubleValue())
                .address(sos.getAddress())
                .dateTime(sos.getDateTime().toString())
                .patientMessage(sos.getPatientMessage())
                .aiReport(sos.getAiReport())
                .status(sos.getStatus().name())
                .build();
    }
}
