package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.symptom.SymptomDiaryEntryDto;

import java.time.LocalDate;
import java.util.List;

public interface SymptomDiaryService {
    /**
     * Crea un nuevo registro en el diario de síntomas
     * @param dto Datos del registro
     * @return ID del recurso Observation creado
     */
    String createDiaryEntry(SymptomDiaryEntryDto dto);

    /**
     * Actualiza un registro existente en el diario de síntomas
     * @param observationId ID del recurso Observation a actualizar
     * @param dto Nuevos datos del registro
     * @return ID del recurso actualizado
     */
    String updateDiaryEntry(String observationId, SymptomDiaryEntryDto dto);

    /**
     * Obtiene todos los registros de síntomas para un paciente
     * @param patientId ID del paciente
     * @return Lista de todos los registros del diario de síntomas
     */
    List<SymptomDiaryEntryDto> getAllPatientSymptomDiaries(String patientId);

    /**
     * Obtiene los registros de síntomas para un paciente en un rango de fechas
     * @param patientId ID del paciente
     * @param startDate Fecha inicial inclusive
     * @param endDate Fecha final inclusive
     * @return Lista de registros del diario de síntomas en el rango especificado
     */
    List<SymptomDiaryEntryDto> getPatientSymptomDiariesByDateRange(
            String patientId, LocalDate startDate, LocalDate endDate);

    /**
     * Obtiene un registro específico del diario de síntomas por su ID
     * @param observationId ID del recurso Observation
     * @return Registro del diario de síntomas
     */
    SymptomDiaryEntryDto getSymptomDiaryById(String observationId);

    /**
     * Elimina un registro del diario de síntomas por su ID
     * @param observationId ID del recurso Observation a eliminar
     */
    void deleteSymptomDiary(String observationId);
}
