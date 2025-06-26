package com.proyecto.appclinica.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SymptomRegistrationResponse {
    private List<String> registrados;
    private List<String> errores;
}
