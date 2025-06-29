package com.proyecto.appclinica.model.dto.treatment;

import lombok.Data;

@Data
public class TreatmentResultDto {
    private boolean success;
    private String message;
    private Long treatmentId;
}
