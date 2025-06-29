package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.EmergencyContactCreateDTO;
import com.proyecto.appclinica.model.dto.EmergencyContactResponseDTO;
import com.proyecto.appclinica.model.dto.MedicationSettingsDTO;
import com.proyecto.appclinica.model.dto.UserSettingsResponseDTO;
import com.proyecto.appclinica.model.entity.UserSettings;
import jakarta.validation.Valid;

import java.util.List;

public interface UserSettingsService {

    UserSettings getUserSettings(Long userId);

    UserSettingsResponseDTO getUserSettingsResponse(Long userId);

    UserSettings createUserSettings(Long userId);

    MedicationSettingsDTO updateMedicationSettings(Long userId, @Valid MedicationSettingsDTO medicationSettingsDTO);

    MedicationSettingsDTO getMedicationSettings(Long userId);

    List<EmergencyContactResponseDTO> getEmergencyContacts(Long userId);

    EmergencyContactResponseDTO addEmergencyContact(Long userId, EmergencyContactCreateDTO contactDTO);

    EmergencyContactResponseDTO updateEmergencyContact(Long userId, Long contactId, EmergencyContactCreateDTO contactDTO);

    void deleteEmergencyContact(Long userId, Long contactId);
}
