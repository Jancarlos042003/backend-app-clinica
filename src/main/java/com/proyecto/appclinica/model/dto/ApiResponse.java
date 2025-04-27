package com.proyecto.appclinica.model.dto;

public record ApiResponse<T>(boolean success, String message, T data) {

    // Constructor adicional para el caso de éxito con datos
    public ApiResponse(T data, String message) {
        this(true, message, data);
    }

    // Constructor adicional para el caso de fallo sin datos
    public ApiResponse(boolean success, String message) {
        this(success, message, null);
    }
}