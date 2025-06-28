package com.proyecto.appclinica.event.patient;

import com.proyecto.appclinica.model.entity.EPatientRecordStatus;
import com.proyecto.appclinica.model.entity.ERole;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.model.entity.RoleEntity;
import com.proyecto.appclinica.repository.PatientRepository;
import com.proyecto.appclinica.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PatientCreatedEventListener {
    private final PatientRepository patientRepository;
    private final RoleService roleService;

    @EventListener
    public PatientEntity createPatient(PatientCreatedEvent event) {
        Set<RoleEntity> roles = new HashSet<>();
        Patient patient = event.getPatient();

        RoleEntity role = roleService.findByName(ERole.PATIENT.name());
        roles.add(role);

        PatientEntity patientEntity = convertPatientToEntity(
                patient,
                roles
        );

        return patientRepository.save(patientEntity);
    }

    private PatientEntity convertPatientToEntity(Patient patient, Set<RoleEntity> roles) {
        // Obtener todos los nombres del paciente y unirlos en un solo String
        StringBuilder nameBuilder = new StringBuilder();
        patient.getNameFirstRep().getGiven().forEach(given -> nameBuilder.append(given).append(" "));

        return PatientEntity.builder()
                .name(nameBuilder.toString().trim())
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
                .password(UUID.randomUUID().toString()) // Generar una contraseña aleatoria temporal
                .gender(patient.getGender().name()) // MALE, FEMALE, OTHER, UNKNOWN
                .roles(roles)
                .status(EPatientRecordStatus.VERIFIED)
                .build();
    }

}
