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
            @ToolParam(description = "Identificador único del paciente") String identifier
    ) {
        if (identifier == null || identifier.isEmpty()) {
            throw new InvalidRequestException("El identificador del paciente no puede ser nulo o vacío.");
        }
        return medicationService.getMedicationsToday(identifier);
    }

    @Tool(name = "get_medications_by_date", description = "Recuperar los medicamentos de una paciente para una fecha específica")
    public List<MedicationResponseDto> getMedicationsByDate(
            @ToolParam(description = "Identificador único del paciente") String identifier,
            @ToolParam(description = "Fecha en formato ISO 8601") String date
    ) {
        if (identifier == null || identifier.isEmpty()) {
            throw new InvalidRequestException("El identificador del paciente no puede ser nulo o vacío.");
        }
        if (date == null || date.isEmpty()) {
            throw new InvalidRequestException("La fecha no puede ser nula o vacía.");
        }
        return medicationService.getMedicationsByDate(identifier, LocalDate.parse(date));
    }

    @Tool(name = "get_medications_in_date_range", description = "Recuperar los medicamentos de una paciente en un rango de fechas")
    public List<MedicationResponseDto> getMedicationsInDateRange(
            @ToolParam(description = "Identificador único del paciente") String identifier,
            @ToolParam(description = "Fecha de inicio en formato ISO 8601") String startDate,
            @ToolParam(description = "Fecha de fin en formato ISO 8601") String endDate
    ) {
        if (identifier == null || identifier.isEmpty()) {
            throw new InvalidRequestException("El identificador del paciente no puede ser nulo o vacío.");
        }
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            throw new InvalidRequestException("Las fechas de inicio y fin no pueden ser nulas o vacías.");
        }
        return medicationService.getMedicationsInDateRange(identifier, LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    @Tool(name = "get_medications_by_date_range_and_status", description = "Recuperar los medicamentos de una paciente en un rango de fechas y estado específico")
    public List<MedicationResponseDto> getMedicationsByDateRangeAndStatus(
            @ToolParam(description = "Identificador único del paciente") String identifier,
            @ToolParam(description = "Fecha de inicio en formato ISO 8601") String startDate,
            @ToolParam(description = "Fecha de fin en formato ISO 8601") String endDate,
            @ToolParam(description = "Estado del medicamento") String status
    ) {
        if (identifier == null || identifier.isEmpty()) {
            throw new InvalidRequestException("El identificador del paciente no puede ser nulo o vacío.");
        }
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            throw new InvalidRequestException("Las fechas de inicio y fin no pueden ser nulas o vacías.");
        }
        return medicationService.getMedicationsByDateRangeAndStatus(identifier, LocalDate.parse(startDate), LocalDate.parse(endDate), status);
    }
}
