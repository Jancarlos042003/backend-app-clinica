package com.proyecto.appclinica.event.sos;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.entity.EmergencyContact;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.model.entity.SosEntity;
import com.proyecto.appclinica.model.entity.UserSettings;
import com.proyecto.appclinica.repository.UserSettingsRepository;
import com.proyecto.appclinica.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertSosCreateEventListener {
    private final UserSettingsRepository userSettingsRepository;
    private final SmsService smsService;

    @EventListener
    @Async
    public void handleAlertSosCreateEvent(AlertSosCreateEvent event) {
        PatientEntity patient = event.getPatient();
        SosEntity sos = event.getSos();

        // Obtenemos los datos necesarios del paciente y del SOS
        String fullName = patient.getName() + " " + patient.getLastname();
        String address = sos.getAddress();
        LocalDateTime dateTime = sos.getDateTime();
        String patientMessage = sos.getPatientMessage() != null ? sos.getPatientMessage() : "No hay mensaje del paciente";

        UserSettings userSettings = userSettingsRepository.findByPatientId(patient.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de usuario no encontrada para el paciente con ID: " + patient.getPatientId()));

        List<EmergencyContact> emergencyContact = userSettings.getEmergencyContacts();

        emergencyContact.forEach(contact -> {
            String message = String.format("""
                            🚨 Alerta SOS del paciente %s: \
                            
                             Nombre Completo: %s \
                            
                             Fecha y Hora: %s \
                            
                             Dirección: %s \
                            
                             Mensaje: %s""",
                    patient.getPatientId(), fullName, dateTime, address, patientMessage);
            smsService.sendSms(contact.getPhoneNumber(), message);
        });
    }
}
