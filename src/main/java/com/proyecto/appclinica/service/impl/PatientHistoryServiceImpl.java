package com.proyecto.appclinica.service.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.proyecto.appclinica.model.dto.PatientHistoryResponse;
import com.proyecto.appclinica.service.PatientHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientHistoryServiceImpl implements PatientHistoryService {
    private final IGenericClient fhirClient;
    private final FhirContext fhirContext;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final DocumentTransformer documentTransformer;

    @Value("classpath:prompts/history-summary")
    private Resource promptHistorySummary;

    @Override
    public PatientHistoryResponse createPatientHistory(String patientId) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusYears(1); // Un año atrás

        return createPatientHistory(patientId, fromDate, today);
    }

    @Override
    public PatientHistoryResponse createPatientHistory(String patientId, LocalDate fromDate, LocalDate toDate) {

        try {
            // Formatear las fechas para FHIR
            String fromDateStr = fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String toDateStr = toDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Obtener el historial del paciente
            Patient patient = fhirClient.read()
                    .resource(Patient.class)
                    .withId(patientId)
                    .execute();

            Bundle conditions = getConditions(patientId);
            Bundle procedures = getProcedures(patientId, fromDateStr, toDateStr);
            Bundle treatments = getTreatments(patientId, fromDateStr, toDateStr);

            // Combinar los recursos obtenidos
            Bundle combinedBundle = combineBundles(conditions, procedures, treatments, patient);

            log.info("Historial del paciente {} desde {} hasta {}: {} recursos encontrados",
                    patientId, fromDate, toDate, combinedBundle.getEntry().size());

            String historyJson = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(combinedBundle);

            // Crear un resumen del historial utilizando el modelo de chat
            String historySummary = createHistorySummary(historyJson);

            // Guardar el resumen en el VectorStore
            saveToVectorStore(historySummary, patientId);

            return PatientHistoryResponse.builder()
                    .patientId(patientId)
                    .message("Historial del paciente creado correctamente.")
                    .build();

        } catch (Exception e) {
            log.error("Error al obtener el historial del paciente {}: {}", patientId, e.getMessage());
            throw new RuntimeException("Error al obtener el historial del paciente", e);
        }
    }

    @Override
    public List<Document> getPatientHistory(String patientId) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query("Historial del paciente " + patientId)
                .filterExpression("patient_id == '" + patientId + "'")
                .build());
    }

    private Bundle getConditions(String patientId) {
        return fhirClient.search()
                .forResource(Condition.class)
                .where(Condition.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
    }

    private Bundle getProcedures(String patientId, String fromDateStr, String toDateStr) {
        return fhirClient.search()
                .forResource(Procedure.class)
                .where(Procedure.PATIENT.hasId(patientId))
                .and(Procedure.DATE.afterOrEquals().day(fromDateStr))
                .and(Procedure.DATE.beforeOrEquals().day(toDateStr))
                .returnBundle(Bundle.class)
                .execute();
    }

    private Bundle getTreatments(String patientId, String fromDateStr, String toDateStr) {
        return fhirClient.search()
                .forResource(Procedure.class)
                .where(Procedure.PATIENT.hasId(patientId))
                .and(Procedure.DATE.afterOrEquals().day(fromDateStr))
                .and(Procedure.DATE.beforeOrEquals().day(toDateStr))
                .returnBundle(Bundle.class)
                .execute();
    }

    private Bundle combineBundles(Bundle conditions, Bundle procedures, Bundle treatments, Patient patient) {
        Bundle combined = new Bundle();
        combined.setType(Bundle.BundleType.COLLECTION);
        combined.addEntry().setResource(patient);

        if (conditions != null) {
            combined.getEntry().addAll(conditions.getEntry());
        }
        if (procedures != null) {
            combined.getEntry().addAll(procedures.getEntry());
        }
        if (treatments != null) {
            combined.getEntry().addAll(treatments.getEntry());
        }

        return combined;
    }

    private String createHistorySummary(String historyJson) {
        return chatClient.prompt(historyJson)
                .system(promptHistorySummary)
                .call()
                .content();
    }

    private void saveToVectorStore(String historySummary, String patientId) {
        List<Document> documents = List.of(
                Document.builder()
                        .text(historySummary)
                        .metadata(Map.of("patient_id", patientId, "category", "patient_history"))
                        .build()
        );

        List<Document> chunks = documentTransformer.apply(documents);
        log.info("Guardando el resumen del historial del paciente {} en el VectorStore", patientId);

        vectorStore.add(chunks);
    }
}
