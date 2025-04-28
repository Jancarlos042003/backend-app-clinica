package com.proyecto.appclinica.model.dto.treatment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTreatmentDto {
    @NotNull(message = "El ID del tratamiento es obligatorio")
    private String id;

    private String nameMedicine;
    private Double doseValue;
    private String doseUnit;
    private Integer frequencyHours;
    private LocalDate startDate;
    private Integer duration;

}
