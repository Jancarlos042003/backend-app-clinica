package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.InvalidRequestException;
import com.proyecto.appclinica.model.dto.DocumentUploadRequest;
import com.proyecto.appclinica.model.dto.DocumentUploadResponse;
import com.proyecto.appclinica.service.DocumentIngestionService;
import com.proyecto.appclinica.util.MultipartFileResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionServiceImpl implements DocumentIngestionService {
    private final VectorStore vectorStore;
    private final DocumentTransformer documentTransformer;
    private final KeywordMetadataEnricher metadataEnricher;

    @Override
    public DocumentUploadResponse documentIngestion(DocumentUploadRequest uploadRequest) {
        MultipartFile file = uploadRequest.getFile();
        String category = uploadRequest.getCategory();
        String specialty = uploadRequest.getSpecialty();

        if (file == null || file.isEmpty()) {
            log.error("El archivo no puede ser nulo o vacío.");
            throw new InvalidRequestException("El archivo no puede ser nulo o vacío.");
        }

        if (!isValidPdf(file)) {
            log.error("El archivo debe ser un PDF válido.");
            throw new InvalidRequestException("El archivo debe ser un PDF válido.");
        }

        Resource resource = new MultipartFileResource(file);

        // Document Reader
        log.info("Iniciando la lectura del documento PDF: {}", file.getOriginalFilename());
        PagePdfDocumentReader documentReader = new PagePdfDocumentReader(resource);
        List<Document> documents = documentReader.get();
        addMetadata(documents, category, specialty);

        // Normalización
        List<Document> normalizedDocuments = normalizeChunks(documents);

        // Document Transformer
        log.info("Dividiendo el documento en fragmentos de texto.");
        List<Document> chunks = documentTransformer.apply(normalizedDocuments);
        log.info("Fragmentos de texto obtenidos: {}", chunks.size());

        // Enriquecimiento de metadatos
        List<Document> enrichedChunks = metadataEnricher.apply(chunks);

        // Document Writer
        log.info("Agregando los fragmentos al VectorStore.");
        vectorStore.add(enrichedChunks);

        return DocumentUploadResponse.builder()
                .message("Documento procesado y agregado al VectorStore correctamente.")
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .build();
    }

    private boolean isValidPdf(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }

    private void addMetadata(List<Document> documents, String category, String specialty) {
        LocalDate publicationDate = LocalDate.now();

        documents.forEach(document -> {
            document.getMetadata().put("ingested_at", publicationDate.toString());
            document.getMetadata().put("category", category);
            if (specialty != null && !specialty.isEmpty()) {
                document.getMetadata().put("specialty", specialty);
            }
        });
    }

    private List<Document> normalizeChunks(List<Document> documents){
        return documents.stream()
                .map(doc -> Document.builder()
                        .id(doc.getId())
                        .text(doc.getText().replaceAll("\\s+", " ").trim())
                        .metadata(doc.getMetadata())
                        .build())
                .toList();
    }
}
