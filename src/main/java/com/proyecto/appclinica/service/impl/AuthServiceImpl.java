package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.event.patient.PatientCreatedEvent;
import com.proyecto.appclinica.exception.InvalidCodeException;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.PatientProfileResponse;
import com.proyecto.appclinica.model.dto.VerificationStatusResponseDto;
import com.proyecto.appclinica.model.dto.auth.CodeSubmissionResponseDto;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeResponse;
import com.proyecto.appclinica.model.entity.EPatientRecordStatus;
import com.proyecto.appclinica.model.entity.PatientEntity;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements  AuthService {

    private final FhirPatientRepository fhirPatientRepository;
    private final CodeVerificationService codeService;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public VerificationStatusResponseDto checkUserExists(String identifier) {
        // Primero verificamos si el usuario existe en el sistema FHIR
        boolean existsInFhir = fhirPatientRepository.patientExistsByIdentifier(identifier);

        if (!existsInFhir) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        // Luego verificamos si el usuario ya ha sido verificado en nuestra base de datos
        Optional<PatientEntity> patient = patientRepository.findByDni(identifier);
        if (patient.isPresent() && patient.get().getStatus() == EPatientRecordStatus.COMPLETE) {
            return new VerificationStatusResponseDto(
                    identifier,
                    EPatientRecordStatus.COMPLETE,
                    "El usuario ya ha completado el registro.",
                    false
            );
        } else if (patient.isPresent() && patient.get().getStatus() == EPatientRecordStatus.VERIFIED) {
            return new VerificationStatusResponseDto(
                    identifier,
                    EPatientRecordStatus.VERIFIED,
                    "El usuario ya ha verificado su identidad. Proceda a configurar las credenciales.",
                    true
            );
        }  else if (patient.isPresent() && patient.get().getStatus() == EPatientRecordStatus.BLOCKED) {
            return new VerificationStatusResponseDto(
                    identifier,
                    EPatientRecordStatus.BLOCKED,
                    "El usuario está bloqueado. No puede proceder con la verificación.",
                    false
            );
        }

        // Si el usuario existe en FHIR, pero no en nuestra BD, se envía el código de verificación
        codeService.generateAndSendCode(identifier);

        return new VerificationStatusResponseDto(
                identifier,
                EPatientRecordStatus.PENDING,
                "Código de verificación enviado.",
                false
        );
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