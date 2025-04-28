package com.proyecto.appclinica.service;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.UserProfileResponse;
import com.proyecto.appclinica.repository.FhirPatientRepository;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final FhirPatientRepository fhirPatientRepository;

    public UserProfileResponse getUserProfile(String identifier) {
        Patient patient = fhirPatientRepository.getPatientByIdentifier(identifier);

        if (patient == null) {
            throw new ResourceNotFoundException("Usuario", "DNI", identifier);
        }

        // Extraer información del paciente
        String id = patient.getIdElement().getIdPart();
        String name = extractPatientName(patient);
        String email = extractPatientEmail(patient);

        return new UserProfileResponse(id, name, email);
    }

    private String extractPatientName(Patient patient) {
        if (patient.hasName()) {
            HumanName name = patient.getNameFirstRep();
            if (name.hasGiven() && name.hasFamily()) {
                return name.getGivenAsSingleString() + " " + name.getFamily();
            } else if (name.hasFamily()) {
                return name.getFamily();
            } else if (name.hasGiven()) {
                return name.getGivenAsSingleString();
            }
        }
        return "Sin nombre";
    }

    private String extractPatientEmail(Patient patient) {
        if (patient.hasTelecom()) {
            return patient.getTelecom().stream()
                    .filter(t -> t.getSystem().name().equals("EMAIL"))
                    .findFirst()
                    .map(ContactPoint::getValue)
                    .orElse("Sin email");
        }
        return "Sin email";
    }
}
