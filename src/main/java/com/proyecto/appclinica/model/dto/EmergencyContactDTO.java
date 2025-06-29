package com.proyecto.appclinica.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactDTO {

    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;

    @NotBlank(message = "El número de teléfono no puede estar vacío")
    @Pattern(regexp = "9\\d{8}", message = "El número de teléfono debe comenzar con 9 y tener 9 dígitos")
    private String phoneNumber;

    private String relationship;
}
