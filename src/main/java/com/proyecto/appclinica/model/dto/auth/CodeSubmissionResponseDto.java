package com.proyecto.appclinica.model.dto.auth;

public record CodeSubmissionResponseDto(
        String message,
        String phoneNumber,
        String email
) {
}
