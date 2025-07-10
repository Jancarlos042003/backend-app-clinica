package com.proyecto.appclinica.model.dto;

import lombok.Builder;

@Builder
public record DocumentUploadResponse(
        String message,
        String fileName,
        String fileType
) {
}
