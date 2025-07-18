package com.proyecto.appclinica.model.dto;

import lombok.Builder;

@Builder
public record PatientProfileResponse(
        String name,
        String lastname,
        String dni,
        String birthDate,
        String phone,
        String email,
        String patientId
) {
}
