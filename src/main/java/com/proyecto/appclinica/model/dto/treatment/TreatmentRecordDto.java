package com.proyecto.appclinica.model.dto.treatment;

import lombok.Data;

@Data
public class TreatmentRecordDto {
    private Long id;
    private String nameMedicine;
    private String dose; // Ejm "500 mg"
    private String frequency; //Ejm "cada 8 horas"
    private String startDate;
    private String endDate;
    private String duration; // Ejm "7 días"
    private String status; // Ejm "activo", "completado", "cancelado"
    private Integer progress; // Porcentaje de progreso del tratamiento
}
