package com.proyecto.appclinica.model.dto.auth;

public record ResponseUserExistsDto(
        boolean exists,
        String message
) {
}
