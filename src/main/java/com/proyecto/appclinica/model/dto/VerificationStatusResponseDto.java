package com.proyecto.appclinica.model.dto;

import com.proyecto.appclinica.model.entity.EPatientRecordStatus;

public record VerificationStatusResponseDto(
        String identifier,  // DNI
        EPatientRecordStatus registrationStatus, // e.g. "VERIFIED", "CREDENTIALS_INCOMPLETE", "COMPLETE", "BLOCKED"
        String message,
        boolean canCreateCredentials
) {
}
