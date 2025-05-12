package com.proyecto.appclinica.model.dto.auth;

import lombok.Builder;

@Builder
public record AuthResponseDto(
        String message,
        String token,
        String refreshToken
) {
}
