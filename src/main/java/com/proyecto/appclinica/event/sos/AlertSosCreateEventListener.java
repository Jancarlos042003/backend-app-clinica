package com.proyecto.appclinica.event.sos;

import com.proyecto.appclinica.model.entity.EmergencyContact;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.model.entity.SosEntity;
import com.proyecto.appclinica.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertSosCreateEventListener {
    private final SmsService smsService;

    @EventListener
    @Async
    public void handleAlertSosCreateEvent(AlertSosCreateEvent event) {

        log.info("Entrando al manejador de eventos AlertSosCreateEvent");

        PatientEntity patient = event.getPatient();
        SosEntity sos = event.getSos();

        // Obtenemos los datos necesarios del paciente y del SOS
        String fullName = patient.getName() + " " + patient.getLastname();
        String dni = patient.getDni();
        String address = sos.getAddress();
        LocalDateTime dateTime = sos.getDateTime();
        String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String patientMessage = sos.getPatientMessage().isEmpty() ? sos.getPatientMessage() : "No hay mensaje del paciente";

        List<EmergencyContact> emergencyContact = event.getUserSettings().getEmergencyContacts();
        log.info("Contactos de emergencia obtenidos: {}", emergencyContact.size());

        if (emergencyContact.isEmpty()) {
            log.warn("No hay contactos de emergencia configurados para el paciente {}", fullName);
            return;
        }

        emergencyContact.forEach(contact -> {
            String message = String.format("""
                            🚨 Alerta SOS del paciente %s: \
                            
                             DNI: %s \
                            
                             Fecha y Hora: %s \
                            
                             Dirección: %s \
                            
                             Mensaje: %s""",
                    fullName, dni, formattedDateTime, address, patientMessage);


            smsService.sendSms(contact.getPhoneNumber(), message);
            log.info("Enviando SMS a {}: {}", contact.getPhoneNumber(), message);
        });
    }
}
