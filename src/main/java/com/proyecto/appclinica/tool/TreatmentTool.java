package com.proyecto.appclinica.tool;

import com.proyecto.appclinica.exception.InvalidRequestException;
import com.proyecto.appclinica.model.dto.treatment.TreatmentRecordDto;
import com.proyecto.appclinica.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TreatmentTool {
    private final TreatmentService treatmentService;

    @Tool(name = "get_treatment_by_status", description = "Obtener tratamientos médicos por estado")
    public List<TreatmentRecordDto> getTreatmentByStatus(
            @ToolParam(description = "Estado del tratamiento (cancelled/ completed/ active)") String status,
            @ToolParam(description = "Identificador único del paciente")  String patientId
    ){
        if (status == null || patientId == null) {
            throw new InvalidRequestException("Los parámetros 'status' e 'identifier' son obligatorios.");
        }

        if (!status.equalsIgnoreCase("cancelled") &&
                !status.equalsIgnoreCase("completed") &&
                !status.equalsIgnoreCase("active")) {
            throw new InvalidRequestException("Estado no válido. Debe ser 'cancelled', 'completed' o 'active'.");
        }

        return treatmentService.getAllMedicationRequestsByPatientIdAndStatus(patientId, status);
    }
}
