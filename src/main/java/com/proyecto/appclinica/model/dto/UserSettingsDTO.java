package com.proyecto.appclinica.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class UserSettingsDTO {

    private Long id;

    @NotNull(message = "El ID de usuario no puede ser nulo")
    private Long userId;

    @Valid
    private MedicationSettingsDTO medicationSettings;

    @Valid
    @Builder.Default
    private List<EmergencyContactDTO> emergencyContacts = new ArrayList<>();
}
