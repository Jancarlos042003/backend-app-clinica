package com.proyecto.appclinica.model.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record PatientProfileResponse(
        String name,
        String lastname,
        String dni,
        Date birthDate,
        String phone,
        String email
) {
}
