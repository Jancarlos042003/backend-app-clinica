package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.symptom.SymptomDto;
import com.proyecto.appclinica.model.dto.symptom.SymptomRecordDto;

import java.time.LocalDate;
import java.util.List;

public interface SymptomDiaryService {
    /**
     * Crea un nuevo registro en el diario de síntomas
     *
     * @param dto Datos del síntoma
     * @return ID del recurso Observation creado
     */
    SymptomRecordDto createDiaryEntry(SymptomDto dto);

    /**
     * Actualiza un registro existente en el diario de síntomas
     *
     * @param observationId ID del recurso Observation a actualizar
     * @param dto           Nuevos datos del síntoma
     * @return ID del recurso actualizado
     */
    SymptomRecordDto updateDiaryEntry(String observationId, SymptomDto dto);

    /**
     * Obtiene todos los registros de síntomas para un paciente
     * @return Lista de todos los síntomas registrados
     */
    List<SymptomRecordDto> getAllPatientSymptomDiaries(String patientId);

    /**
     * Obtiene los registros de síntomas para un paciente en un rango de fechas
     * @param patientId ID del paciente
     * @param startDate Fecha inicial inclusive
     * @param endDate Fecha final inclusive
     * @return Lista de síntomas registrados en el rango especificado
     */
    List<SymptomRecordDto> getPatientSymptomDiariesByDateRange(String patientId, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene los síntomas registrados por un paciente en el día actual
     * @param patientId ID del paciente
     * @return Lista de síntomas registrados hoy
     */
    List<SymptomRecordDto> getTodaySymptomsByPatient(String patientId);

    /**
     * Obtiene un registro específico de síntoma por su ID
     * @param observationId ID del recurso Observation
     * @return Datos del síntoma registrado
     */
    SymptomRecordDto getSymptomDiaryById(String observationId);

    /**
     * Elimina un registro de síntoma por su ID
     * @param observationId ID del recurso Observation a eliminar
     */
    void deleteSymptomDiary(String observationId);

    /**
     * Obtiene los síntomas registrados por un paciente en el día actual (por fecha de creación)
     * @param patientId ID del paciente
     * @return Lista de síntomas registrados hoy en el sistema
     */
    List<SymptomRecordDto> getTodayRegisteredSymptomsByPatient(String patientId);

    /**
     * Obtiene los registros de síntomas para un paciente en un rango de fechas (por fecha de registro)
     * @param patientId ID del paciente
     * @param startDate Fecha inicial inclusive
     * @param endDate Fecha final inclusive
     * @return Lista de síntomas registrados en el sistema en el rango especificado
     */
    List<SymptomRecordDto> getPatientSymptomDiariesByRegistrationDateRange(String patientId, LocalDate startDate, LocalDate endDate);
}
