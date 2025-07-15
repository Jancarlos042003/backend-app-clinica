package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.SosRequest;
import com.proyecto.appclinica.model.dto.SosResponse;
import com.proyecto.appclinica.model.dto.SosUpdateRequest;
import com.proyecto.appclinica.model.entity.ESosStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SosService {
    SosResponse createSos(SosRequest sosRequest);

    SosResponse getSosById(Long id);

    SosResponse updateSos(Long id, SosUpdateRequest sosRequest);

    String deleteSos(Long id);

    List<SosResponse> getSosByPatientId(String patientId);

    List<SosResponse> getAllSos();

    List<SosResponse> getSosByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<SosResponse> getSosByStatus(ESosStatus status);
}
