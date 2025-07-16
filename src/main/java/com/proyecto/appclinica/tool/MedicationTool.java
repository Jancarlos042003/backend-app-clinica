package com.proyecto.appclinica.tool;

import com.proyecto.appclinica.exception.InvalidRequestException;
import com.proyecto.appclinica.model.dto.treatment.medication.MedicationResponseDto;
import com.proyecto.appclinica.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MedicationTool {
    private final MedicationService medicationService;

    @Tool(name = "get_medications_today", description = "Recuperar los medicamentos de hoy para una paciente")
    public List<MedicationResponseDto> getMedicationsToday(
            @ToolParam(description = "ID del paciente") String patientId
    ) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("El ID del paciente no puede ser nulo o vacío.");
        }
        return medicationService.getMedicationsToday(patientId);
    }

    @Tool(name = "get_medications_by_date", description = "Recuperar los medicamentos de una paciente para una fecha específica")
    public List<MedicationResponseDto> getMedicationsByDate(
            @ToolParam(description = "ID del paciente") String patientId,
            @ToolParam(description = "Fecha en formato ISO 8601") String date
    ) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("ID del paciente no puede ser nulo o vacío.");
        }
        if (date == null || date.isEmpty()) {
            throw new InvalidRequestException("La fecha no puede ser nula o vacía.");
        }
        return medicationService.getMedicationsByDate(patientId, LocalDate.parse(date));
    }

    @Tool(name = "get_medications_in_date_range", description = "Recuperar los medicamentos de una paciente en un rango de fechas")
    public List<MedicationResponseDto> getMedicationsInDateRange(
            @ToolParam(description = "ID del paciente") String patientId,
            @ToolParam(description = "Fecha de inicio en formato ISO 8601") String startDate,
            @ToolParam(description = "Fecha de fin en formato ISO 8601") String endDate
    ) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("El identificador del paciente no puede ser nulo o vacío.");
        }
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            throw new InvalidRequestException("Las fechas de inicio y fin no pueden ser nulas o vacías.");
        }
        return medicationService.getMedicationsInDateRange(patientId, LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    @Tool(name = "get_medications_by_date_range_and_status", description = "Recuperar los medicamentos de una paciente en un rango de fechas y estado específico")
    public List<MedicationResponseDto> getMedicationsByDateRangeAndStatus(
            @ToolParam(description = "ID del paciente") String patientId,
            @ToolParam(description = "Fecha de inicio en formato ISO 8601") String startDate,
            @ToolParam(description = "Fecha de fin en formato ISO 8601") String endDate,
            @ToolParam(description = "Estado del medicamento") String status
    ) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("ID del paciente no puede ser nulo o vacío.");
        }
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            throw new InvalidRequestException("Las fechas de inicio y fin no pueden ser nulas o vacías.");
        }
        return medicationService.getMedicationsByDateRangeAndStatus(patientId, LocalDate.parse(startDate), LocalDate.parse(endDate), status);
    }
}
