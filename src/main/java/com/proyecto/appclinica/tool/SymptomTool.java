package com.proyecto.appclinica.tool;

import com.proyecto.appclinica.model.dto.SymptomInput;
import com.proyecto.appclinica.model.dto.SymptomRegistrationResponse;
import com.proyecto.appclinica.model.dto.symptom.SymptomDto;
import com.proyecto.appclinica.model.dto.symptom.SymptomRecordDto;
import com.proyecto.appclinica.service.SymptomDiaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SymptomTool {
    private final SymptomDiaryService symptomDiaryService;

    @Tool(name = "register_symptom", description = "Registra uno o más síntomas clínicos del paciente")
    @Transactional
    public SymptomRegistrationResponse register_symptom(
            @ToolParam(description = "Lista de síntomas a procesar") List<SymptomInput> symptoms,
            String identifier
    ) {
        // Crear respuesta inicial
        SymptomRegistrationResponse response = new SymptomRegistrationResponse();
        response.setRegistrados(new ArrayList<>());
        response.setErrores(new ArrayList<>());

        // Validar entrada
        if (symptoms == null || symptoms.isEmpty()) {
            response.getErrores().add("No se recibió ningún síntoma.");
            return response;
        }

        for (SymptomInput input : symptoms) {
            String symptom = input.getSymptom();
            String intensity = input.getIntensity();

            if (symptom == null || symptom.isBlank()) {
                response.getErrores().add("Síntoma sin nombre.");
                continue;
            }

            if (intensity == null || intensity.isBlank()) {
                response.getErrores().add("Falta intensidad para '" + symptom + "'.");
                continue;
            }

            try {
                // Convertir el input a SymptomDto que usa el servicio
                SymptomDto symptomDto = new SymptomDto();
                symptomDto.setSymptom(symptom);
                symptomDto.setIntensity(intensity);
                symptomDto.setIdentifier(identifier);

                // Manejar la fecha si está presente
                if (input.getStartTime() != null && !input.getStartTime().isBlank()) {
                    symptomDto.setDate(input.getStartTime());
                }

                // Manejar notas si están presentes
                if (input.getNotes() != null && !input.getNotes().isBlank()) {
                    symptomDto.setNotes(input.getNotes());
                }

                // Utilizar el servicio para crear la entrada de diario
                SymptomRecordDto recordDto = symptomDiaryService.createDiaryEntry(symptomDto);

                log.info("Síntoma registrado: {} con intensidad {}, ID: {}", symptom, intensity, recordDto.getId());
                response.getRegistrados().add(symptom + " (" + intensity + ")");
            } catch (Exception e) {
                log.error("Error al registrar síntoma: {}", symptom, e);
                response.getErrores().add("Error al registrar '" + symptom + "': " + e.getMessage());
            }
        }

        return response;
    }

    @Tool(name = "get_patient_symptoms", description = "Obtiene los síntomas registrados del paciente actual.")
    public List<SymptomRecordDto> get_patient_symptoms(String identifier) {
        try {
            // Usar el servicio para obtener todos los síntomas del paciente
            return symptomDiaryService.getAllPatientSymptomDiaries(identifier);
        } catch (Exception e) {
            log.error("Error al obtener síntomas del paciente: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Tool(name = "get_today_symptoms", description = "Obtiene los síntomas registrados del paciente en el día actual.")
    public List<SymptomRecordDto> get_today_symptoms(String identifier) {
        try {
            // Usar el servicio para obtener los síntomas del día actual
            return symptomDiaryService.getTodaySymptomsByPatient(identifier);
        } catch (Exception e) {
            log.error("Error al obtener síntomas del día actual: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Tool(name = "get_symptoms_by_date_range", description = "Obtiene los síntomas registrados del paciente en un rango de fechas. " +
            "Formato de fecha: yyyy-MM-dd")
    public List<SymptomRecordDto> get_symptoms_by_date_range(String startDate, String endDate, String identifier) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            return symptomDiaryService.getPatientSymptomDiariesByDateRange(identifier, start, end);
        } catch (DateTimeParseException e) {
            log.error("Error en formato de fechas: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error al obtener síntomas por rango de fechas: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
