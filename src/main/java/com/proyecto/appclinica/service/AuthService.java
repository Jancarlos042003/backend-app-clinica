package com.proyecto.appclinica.service;

import com.proyecto.appclinica.exception.InvalidCodeException;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.auth.AuthResponseDto;
import com.proyecto.appclinica.model.dto.auth.ResponseUserExistsDto;
import com.proyecto.appclinica.repository.FhirPatientRepository;
import com.proyecto.appclinica.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FhirPatientRepository fhirPatientRepository;
    private final CodeVerificationService codeService;
    private final SecurityUtils securityUtils;

    public boolean userExists(String identifier) {
        return fhirPatientRepository.patientExistsByIdentifier(identifier);
    }

    public ResponseUserExistsDto checkUserExists(String identifier) {
        boolean exists = userExists(identifier);
        return new ResponseUserExistsDto(exists, exists ? "Usuario encontrado" : "Usuario no encontrado");
    }

    public AuthResponseDto verifyCodeAndGetToken(String identifier, String code) {
        // Verificar que el usuario existe
        if (!userExists(identifier)) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        // Verificar el código
        boolean isValid = codeService.verifyCode(identifier, code);
        if (!isValid) {
            throw new InvalidCodeException("Código inválido o expirado");
        }

        // Generar token JWT
        String token = securityUtils.createTokenFromUsername(identifier);

        return new AuthResponseDto("Código verificado correctamente", token);
    }
}