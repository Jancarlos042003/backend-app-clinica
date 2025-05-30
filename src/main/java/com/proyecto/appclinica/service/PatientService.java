package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.PatientProfileResponse;

public interface PatientService {
    PatientProfileResponse getPatient(String identifier);
}
