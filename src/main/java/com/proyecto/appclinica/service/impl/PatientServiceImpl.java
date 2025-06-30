package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.PatientProfileResponse;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.repository.PatientRepository;
import com.proyecto.appclinica.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public PatientProfileResponse getPatient(String identifier) {
        PatientEntity patient = patientRepository.findByDni(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con DNI: " + identifier));

        return PatientProfileResponse.builder()
                .name(patient.getName())
                .lastname(patient.getLastname())
                .dni(patient.getDni())
                .birthDate(patient.getBirthDate())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .patientId(patient.getPatientId())
                .build();
    }
}
