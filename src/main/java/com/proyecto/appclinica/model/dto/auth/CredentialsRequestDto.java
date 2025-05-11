package com.proyecto.appclinica.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredentialsRequestDto {
    @NotBlank(message = "El DNI es obligatorio")
    private String identifier;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotBlank(message = "La confirmación de la contraseña es obligatoria")
    private String confirmPassword;
}
