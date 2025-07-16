package com.proyecto.appclinica.tool;

import com.proyecto.appclinica.model.dto.SosResponse;
import com.proyecto.appclinica.service.SosService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SosTool {
    private final SosService sosService;

    @Tool(name = "get_all_sos_by_patient_id", description = "Obtiene todos los registros SOS de un paciente")
    public List<SosResponse> getAllSosByPatientId(String patientId) {
        return sosService.getSosByPatientId(patientId);
    }
}
