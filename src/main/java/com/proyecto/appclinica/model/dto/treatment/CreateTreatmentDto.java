package com.proyecto.appclinica.model.dto.treatment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTreatmentDto {
    @NotNull(message = "El nombre de la medicina es obligatorio")
    private String nameMedicine;

    @NotNull(message = "El valor de la dosis es obligatorio")
    private Double doseValue;

    @NotNull(message = "La unidad de la dosis es obligatorio")
    private String doseUnit;

    @NotNull(message = "La frecuencia es obligatorio")
    private Integer frequencyHours;

    @NotNull(message = "La fecha de inicio es obligatorio")
    private LocalDate startDate;

    @NotNull(message = "La duracion es obligatorio")
    private Integer duration;

    @NotNull(message = "El ID del paciente es obligatorio")
    private Integer patientId;
}
