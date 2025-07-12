package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.PatientHistoryResponse;
import org.springframework.ai.document.Document;

import java.time.LocalDate;
import java.util.List;

public interface PatientHistoryService {

    /**
     * Obtiene el historial del paciente en formato JSON.
     *
     * @param patientId ID del paciente
     * @return Historial del paciente en formato JSON
     */
    PatientHistoryResponse createPatientHistory(String patientId);

    /**
     * Obtiene el historial del paciente en formato JSON.
     * @param patientId ID del paciente
     * @param fromDate Fecha de inicio del historial
     * @param toDate Fecha de fin del historial
     * @return Historial del paciente en formato JSON
     */
    PatientHistoryResponse createPatientHistory(String patientId, LocalDate fromDate, LocalDate toDate);

    /**
     * Obtiene el historial del paciente en formato JSON.
     *
     * @param patientId ID del paciente
     * @return Lista de documentos que representan el historial del paciente
     */
    List<Document> getPatientHistory(String patientId);
}


