package com.proyecto.appclinica.model.dto;

import lombok.Builder;

@Builder
public record SosResponse(
    Long id,
    String patientId,
    Double latitude,
    Double longitude,
    String address,
    String dateTime,
    String patientMessage,
    String aiReport,
    String status
) {
}
