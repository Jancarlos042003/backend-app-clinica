package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.PatientProfileResponse;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.repository.PatientRepository;
import com.proyecto.appclinica.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public PatientProfileResponse getPatient(String identifier) {

        log.info("Buscando paciente con identificador: {}", identifier);

        PatientEntity patient = patientRepository.findByDni(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con DNI: " + identifier));

        log.info("Paciente encontrado: {}", patient.getName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedBirthDate = patient.getBirthDate() != null ? dateFormat.format(patient.getBirthDate()) : null;

        return PatientProfileResponse.builder()
                .name(patient.getName())
                .lastname(patient.getLastname())
                .dni(patient.getDni())
                .birthDate(formattedBirthDate)
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .patientId(patient.getPatientId())
                .build();
    }
}
