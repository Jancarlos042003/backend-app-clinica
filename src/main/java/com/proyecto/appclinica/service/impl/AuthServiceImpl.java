package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.auth.CodeSubmissionResponseDto;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeResponse;
import com.proyecto.appclinica.repository.FhirPatientRepository;
import com.proyecto.appclinica.repository.PatientRepository;
import com.proyecto.appclinica.service.AuthService;
import com.proyecto.appclinica.service.CodeVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final FhirPatientRepository fhirPatientRepository;
    private final CodeVerificationService codeService;
    private final PatientRepository patientRepository;

    @Override
    public CodeSubmissionResponseDto checkUserExists(String identifier) {
        boolean exists = userExists(identifier);

        if (!exists) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        if (patientRepository.findByDni(identifier).isPresent()){
            throw new IllegalArgumentException("El usuario ya existe. Inicia sesión.");
        }

        // Si el usuario existe, se envía el código de verificación
        return codeService.generateAndSendCode(identifier);
    }

    @Override
    public VerifyCodeResponse verifyCode(String identifier, String code) {
        if (!userExists(identifier)) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        boolean result = codeService.verifyCode(identifier, code);

        if (!result) {
            throw new IllegalArgumentException("El código es incorrecto");
        }

        return new VerifyCodeResponse("El código es correcto");
    }

    @Override
    public CodeSubmissionResponseDto resendCode(String identifier) {
        if (!userExists(identifier)) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        return codeService.resendVerificationCode(identifier);
    }

    private boolean userExists(String identifier) {
        return fhirPatientRepository.patientExistsByIdentifier(identifier);
    }
}