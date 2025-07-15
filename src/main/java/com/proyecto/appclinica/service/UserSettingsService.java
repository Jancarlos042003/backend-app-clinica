package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.EmergencyContactCreateDTO;
import com.proyecto.appclinica.model.dto.EmergencyContactResponseDTO;
import com.proyecto.appclinica.model.dto.MedicationSettingsDTO;
import com.proyecto.appclinica.model.dto.UserSettingsResponseDTO;
import com.proyecto.appclinica.model.entity.UserSettings;
import jakarta.validation.Valid;

import java.util.List;

public interface UserSettingsService {

    UserSettings getUserSettings(String patientId);

    UserSettingsResponseDTO getUserSettingsResponse(String patientId);

    UserSettings createUserSettings(String patientId);

    MedicationSettingsDTO updateMedicationSettings(String patientId, @Valid MedicationSettingsDTO medicationSettingsDTO);

    MedicationSettingsDTO getMedicationSettings(String patientId);

    List<EmergencyContactResponseDTO> getEmergencyContacts(String patientId);

    EmergencyContactResponseDTO addEmergencyContact(String patientId, EmergencyContactCreateDTO contactDTO);

    EmergencyContactResponseDTO updateEmergencyContact(String patientId, Long contactId, EmergencyContactCreateDTO contactDTO);

    void deleteEmergencyContact(String patientId, Long contactId);
}
