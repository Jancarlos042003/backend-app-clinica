package com.proyecto.appclinica.tool;

import com.proyecto.appclinica.model.dto.EmergencyContactResponseDTO;
import com.proyecto.appclinica.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmergencyContactTool {

    private final UserSettingsService userSettingsService;

    @Tool(name = "get_patient_emergency_contacts", description = "Obtiene los contactos de emergencia del paciente.")
    public List<EmergencyContactResponseDTO> getPatientEmergencyContacts(String patientId) {
        return userSettingsService.getEmergencyContacts(patientId);
    }
}
