package com.proyecto.appclinica.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentifierRequest {
    @NotBlank(message = "El identificador es obligatorio")
    private String identifier;
}