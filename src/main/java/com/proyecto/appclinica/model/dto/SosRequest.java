package com.proyecto.appclinica.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SosRequest {
    @NotNull(message = "El ID del paciente no puede ser nulo")
    private String patientId;

    @NotNull(message = "La latitud no puede ser nula")
    private BigDecimal latitude;

    @NotNull(message = "La longitud no puede ser nula")
    private BigDecimal longitude;

    @NotNull(message = "La dirección no puede ser nula")
    private String address;

    private String patientMessage;
}
