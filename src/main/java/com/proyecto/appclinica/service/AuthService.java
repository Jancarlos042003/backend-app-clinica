package com.proyecto.appclinica.service;

import com.proyecto.appclinica.exception.ApiException;
import com.proyecto.appclinica.repository.FhirRepository;
import com.proyecto.appclinica.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FhirRepository fhirRepository;
    private final CodeVerificationService codeService;
    private final SecurityUtils securityUtils;

    public boolean userExists(String identifier) {
        return fhirRepository.patientExistsByIdentifier(identifier);
    }

    public String verifyCodeAndGetToken(String identifier, String code) {
        // Verificar que el usuario existe
        if (!userExists(identifier)) {
            throw new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }

        // Verificar el código
        boolean isValid = codeService.verifyCode(identifier, code);
        if (!isValid) {
            throw new ApiException("Código inválido o expirado", HttpStatus.BAD_REQUEST);
        }

        // Generar token JWT
        return securityUtils.createTokenFromUsername(identifier);
    }
}