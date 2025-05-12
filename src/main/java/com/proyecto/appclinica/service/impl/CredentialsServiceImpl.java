package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.auth.CredentialsRequestDto;
import com.proyecto.appclinica.model.entity.ERole;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.model.entity.RoleEntity;
import com.proyecto.appclinica.repository.FhirPatientRepository;
import com.proyecto.appclinica.repository.PatientRepository;
import com.proyecto.appclinica.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CredentialsServiceImpl implements CredentialsService {

    private final FhirPatientRepository fhirPatientRepository;
    private final PatientRepository patientRepository;
    private final RoleServiceImpl roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createCredentials(CredentialsRequestDto credentialsRequestDto) {
        Set<RoleEntity> roles = new HashSet<>();

        boolean exists = fhirPatientRepository.patientExistsByIdentifier(credentialsRequestDto.getIdentifier());

        if (!exists) {
            throw new ResourceNotFoundException("Usuario", "DNI", credentialsRequestDto.getIdentifier());
        }

        if (!(Objects.equals(credentialsRequestDto.getPassword(), credentialsRequestDto.getConfirmPassword()))) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        RoleEntity role = roleService.findByName(ERole.PATIENT.name());
        roles.add(role);

        Patient patient = fhirPatientRepository.getPatientByIdentifier(credentialsRequestDto.getIdentifier());
        PatientEntity newPatient = convertPatientToEntity(patient, passwordEncoder.encode(credentialsRequestDto.getPassword()), roles);
        patientRepository.save(newPatient);
    }

    private PatientEntity convertPatientToEntity(Patient patient, String password, Set<RoleEntity> roles) {
        return PatientEntity.builder()
                .name(patient.getNameFirstRep().getNameAsSingleString())
                .lastname(patient.getNameFirstRep().getFamily())
                .dni(patient.getIdentifierFirstRep().getValue())
                .birthDate(patient.getBirthDate())
                .phone(patient.getTelecom().stream()
                        .filter(t -> "phone".equals(t.getSystem().toCode()))
                        .findFirst()
                        .map(ContactPoint::getValue)
                        .orElse(null))
                .email(patient.getTelecom().stream()
                        .filter(t -> "email".equals(t.getSystem().toCode()))
                        .findFirst()
                        .map(ContactPoint::getValue)
                        .orElse(null))
                .password(password)
                .roles(roles)
                .build();
    }
}
