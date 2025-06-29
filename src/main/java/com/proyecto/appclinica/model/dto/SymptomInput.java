package com.proyecto.appclinica.model.dto;

import lombok.Data;

@Data
public class SymptomInput {
    private String symptom;
    private String intensity;
    private String startTime; // opcional
    private String notes;     // opcional
}
