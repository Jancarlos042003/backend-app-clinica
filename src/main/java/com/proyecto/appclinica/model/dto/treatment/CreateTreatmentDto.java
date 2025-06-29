package com.proyecto.appclinica.model.dto.treatment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @NotNull(message = "La frecuencia es obligatoria")
    private Integer frequency;

    @NotNull(message = "El periodo es obligatorio")
    private Integer period;

    @NotNull(message = "La unidad del periodo es obligatoria")
    private String periodUnit;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startDate;

    @NotNull(message = "La duración es obligatoria")
    private Integer duration;

    @NotNull(message = "La unidad de duración es obligatoria")
    private String durationUnit;

    private String identifier;

    // Campos para horarios irregulares
    private Boolean isIrregular = false;

    // Lista de patrones de horarios para medicación irregular (MORNING, NOON, AFTERNOON, EVENING, NIGHT)
    private List<String> schedulePatterns;

    // Campo para horarios personalizados con horas específicas
    private Boolean hasCustomTimes = false;

    // Lista de horarios personalizados en formato "HH:mm" (por ejemplo: "08:15", "14:30", "22:00")
    private List<String> customTimes;
}
