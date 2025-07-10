package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.DocumentUploadRequest;
import com.proyecto.appclinica.model.dto.DocumentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentIngestionService {
    DocumentUploadResponse documentIngestion(DocumentUploadRequest uploadRequest);
}
