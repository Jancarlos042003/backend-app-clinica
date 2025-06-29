package com.proyecto.appclinica.model.dto.symptom;

import lombok.Data;

@Data
public class SymptomRecordDto {
    private String id;
    private String date;
    private String symptom;
    private String intensity;
    private String notes;
}
