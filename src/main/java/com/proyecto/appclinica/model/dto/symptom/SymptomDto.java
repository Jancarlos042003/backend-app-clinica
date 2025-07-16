package com.proyecto.appclinica.model.dto.symptom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomDto {
    @NotBlank(message = "El síntoma es obligatorio")
    private String symptom;  // Síntoma obligatorio

    @NotNull(message = "La intensidad es obligatoria")
    private String intensity;  // Intensidad obligatoria

    private String date; // Fecha de inicio

    private Integer duration;  // Duración en minutos/horas

    @Size(max = 1000)
    private String notes;  // Notas adicionales

    private String patientId;  // ID del paciente al que pertenece el síntoma
}
