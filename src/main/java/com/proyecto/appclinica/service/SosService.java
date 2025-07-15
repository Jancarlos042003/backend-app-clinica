package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.SosRequest;
import com.proyecto.appclinica.model.dto.SosResponse;
import com.proyecto.appclinica.model.dto.SosUpdateRequest;

import java.util.List;

public interface SosService {
    SosResponse createSos(SosRequest sosRequest);

    SosResponse getSosById(Long id);

    SosResponse updateSos(Long id, SosUpdateRequest sosRequest);

    String deleteSos(Long id);

    List<SosResponse> getSosByPatientId(String patientId);
}
