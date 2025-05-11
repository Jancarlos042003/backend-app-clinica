package com.proyecto.appclinica.model.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequestDto(
        @NotBlank(message = "El nombre del rol es obligatorio")
        String name
) {
}
