package com.proyecto.appclinica.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatientHistoryTool {
    private final VectorStore vectorStore;

    @Tool(name = "get_patient_history_documents",
            description = "Obtiene los documentos vectoriales que contienen el historial médico del paciente")
    public List<Document> getPatientHistoryDocuments(String patientId) {
        log.info("Obteniendo documentos de historial médico para el paciente con ID: {}", patientId);
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query("Historial del paciente " + patientId)
                .filterExpression("patient_id == '" + patientId + "'")
                .build());
    }
}
