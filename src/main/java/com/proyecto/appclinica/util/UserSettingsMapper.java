package com.proyecto.appclinica.util;

import com.proyecto.appclinica.model.dto.EmergencyContactCreateDTO;
import com.proyecto.appclinica.model.dto.EmergencyContactResponseDTO;
import com.proyecto.appclinica.model.dto.MedicationSettingsDTO;
import com.proyecto.appclinica.model.dto.UserSettingsResponseDTO;
import com.proyecto.appclinica.model.entity.EmergencyContact;
import com.proyecto.appclinica.model.entity.MedicationSettings;
import com.proyecto.appclinica.model.entity.UserSettings;

import java.util.stream.Collectors;

/**
 * Clase utilitaria para mapear entre DTOs y entidades
 */
public class UserSettingsMapper {

    private UserSettingsMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static MedicationSettings toEntity(MedicationSettingsDTO dto) {
        if (dto == null) return null;

        return MedicationSettings.builder()
                .toleranceWindowMinutes(dto.getToleranceWindowMinutes())
                .reminderFrequencyMinutes(dto.getReminderFrequencyMinutes())
                .maxReminderAttempts(dto.getMaxReminderAttempts())
                .build();
    }

    public static MedicationSettingsDTO toDto(MedicationSettings entity) {
        if (entity == null) return null;

        return MedicationSettingsDTO.builder()
                .toleranceWindowMinutes(entity.getToleranceWindowMinutes())
                .reminderFrequencyMinutes(entity.getReminderFrequencyMinutes())
                .maxReminderAttempts(entity.getMaxReminderAttempts())
                .build();
    }

    public static EmergencyContact toEntity(EmergencyContactCreateDTO dto) {
        if (dto == null) return null;

        return EmergencyContact.builder()
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .relationship(dto.getRelationship())
                .build();
    }

    public static EmergencyContactResponseDTO toResponseDto(EmergencyContact entity) {
        if (entity == null) return null;

        return EmergencyContactResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .relationship(entity.getRelationship())
                .build();
    }

    public static UserSettingsResponseDTO toResponseDto(UserSettings entity) {
        if (entity == null) return null;

        return UserSettingsResponseDTO.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .medicationSettings(toDto(entity.getMedicationSettings()))
                .emergencyContacts(
                        entity.getEmergencyContacts().stream()
                                .map(UserSettingsMapper::toResponseDto)
                                .toList()
                )
                .build();
    }
}
