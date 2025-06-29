package com.proyecto.appclinica.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponseDTO {
    private Long id;
    private Long userId;
    private MedicationSettingsDTO medicationSettings;

    @Builder.Default
    private List<EmergencyContactResponseDTO> emergencyContacts = new ArrayList<>();
}
