package com.proyecto.appclinica.model.dto;

import lombok.Builder;

@Builder
public record PatientHistoryResponse(
        String patientId,
        String message
) {
}
