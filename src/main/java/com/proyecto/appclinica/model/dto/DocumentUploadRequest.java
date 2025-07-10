package com.proyecto.appclinica.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUploadRequest {
    @NotNull(message = "El archivo es requerido")
    private MultipartFile file;

    @NotNull(message = "El título es requerido")
    private String category;

    private String specialty; // Opcional
}
