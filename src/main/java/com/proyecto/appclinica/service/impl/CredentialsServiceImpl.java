package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.InvalidRequestException;
import com.proyecto.appclinica.exception.PasswordMismatchException;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.auth.CredentialsRequestDto;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.repository.PatientRepository;
import com.proyecto.appclinica.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CredentialsServiceImpl implements CredentialsService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createCredentials(CredentialsRequestDto credentialsRequestDto) {
        String password = credentialsRequestDto.getPassword();
        String confirmPassword = credentialsRequestDto.getConfirmPassword();

        // Validar que los campos de contraseña no estén vacíos
        if (!StringUtils.hasText(password)) {
            throw new InvalidRequestException("La contraseña no puede estar vacía");
        }

        if (!StringUtils.hasText(confirmPassword)) {
            throw new InvalidRequestException("La confirmación de contraseña no puede estar vacía");
        }

        // Validar que las contraseñas coincidan
        if (!(Objects.equals(password, confirmPassword))) {
            throw new PasswordMismatchException("Las contraseñas no coinciden");
        }

        String identifier = credentialsRequestDto.getIdentifier();

        PatientEntity patient = patientRepository.findByDni(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "DNI", identifier));

        // Asignamos la contraseña al paciente
        patient.setPassword(passwordEncoder.encode(password));

        patientRepository.save(patient);
    }
}
