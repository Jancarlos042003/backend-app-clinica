package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.auth.ResponseUserExistsDto;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeResponse;
import com.proyecto.appclinica.repository.FhirPatientRepository;
import com.proyecto.appclinica.service.AuthService;
import com.proyecto.appclinica.service.CodeVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final FhirPatientRepository fhirPatientRepository;
    private final CodeVerificationService codeService;

    @Override
    public ResponseUserExistsDto checkUserExists(String identifier) {
        boolean exists = userExists(identifier);
        return new ResponseUserExistsDto(exists, exists ? "Usuario encontrado" : "Usuario no encontrado");
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

    private boolean userExists(String identifier) {
        return fhirPatientRepository.patientExistsByIdentifier(identifier);
    }
}