package com.proyecto.appclinica.model.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class SymptomInput {
    @ToolParam(description = "Nombre del síntoma")
    private String symptom;

    @ToolParam(description = "Intensidad del síntoma ( Leve/ Moderada/ Severa)")
    private String intensity;

    @ToolParam(description = "fecha/hora de inicio en formato ISO 8601 si no especifica establece la hora actual",
            required = false)
    private String startTime; // opcional

    @ToolParam(description = "Observaciones adicionales", required = false)
    private String notes;     // opcional
}
