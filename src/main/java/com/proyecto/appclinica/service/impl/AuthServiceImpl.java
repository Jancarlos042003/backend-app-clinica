package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.event.patient.PatientCreatedEvent;
import com.proyecto.appclinica.exception.InvalidCodeException;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.PatientProfileResponse;
import com.proyecto.appclinica.model.dto.auth.CodeSubmissionResponseDto;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeResponse;
import com.proyecto.appclinica.repository.FhirPatientRepository;
import com.proyecto.appclinica.repository.PatientRepository;
import com.proyecto.appclinica.service.AuthService;
import com.proyecto.appclinica.service.CodeVerificationService;
import com.proyecto.appclinica.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements  AuthService {

    private final FhirPatientRepository fhirPatientRepository;
    private final CodeVerificationService codeService;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CodeSubmissionResponseDto checkUserExists(String identifier) {
        // Primero verificamos si el usuario existe en el sistema FHIR
        boolean existsInFhir = fhirPatientRepository.patientExistsByIdentifier(identifier);

        if (!existsInFhir) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        // Luego verificamos si ya existe en nuestra base de datos local
        if (patientRepository.findByDni(identifier).isPresent()){
            throw new InvalidCodeException("El usuario ya existe. Inicia sesión.");
        }

        // Si el usuario existe en FHIR pero no en nuestra BD local, se envía el código de verificación
        return codeService.generateAndSendCode(identifier);
    }

    @Override
    public VerifyCodeResponse verifyCode(String identifier, String code) {
        if (!userExists(identifier)) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        boolean result = codeService.verifyCode(identifier, code);

        if (!result) {
            throw new InvalidCodeException("El código es incorrecto");
        }

        // Si el código es correcto, obtenemos el paciente de FHIR y publicamos el evento
        Patient patient = fhirPatientRepository.getPatientByIdentifier(identifier);
        eventPublisher.publishEvent(new PatientCreatedEvent(patient));

        return new VerifyCodeResponse("El código es correcto");
    }

    @Override
    public CodeSubmissionResponseDto resendCode(String identifier) {
        if (!userExists(identifier)) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        return codeService.resendVerificationCode(identifier);
    }

    // Devolvemos el username(dni) del usuario autenticado
    @Override
    public PatientProfileResponse getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            String identifier = userDetails.getUsername();
            return patientService.getPatient(identifier);
        }
        throw new ResourceNotFoundException("usuario autenticado");
    }

    private boolean userExists(String identifier) {
        return fhirPatientRepository.patientExistsByIdentifier(identifier);
    }
}