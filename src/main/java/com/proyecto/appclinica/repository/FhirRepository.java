package com.proyecto.appclinica.repository;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FhirRepository {
    private final IGenericClient fhirClient;

    public boolean patientExistsByIdentifier(String identifier) {
        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().code(identifier))
                .returnBundle(Bundle.class)
                .execute();

        return !bundle.getEntry().isEmpty();
    }

    public Patient getPatientByIdentifier(String identifier) {
        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().code(identifier))
                .returnBundle(Bundle.class)
                .execute();

        if (bundle.getEntry().isEmpty()) {
            return null;
        }

        return (Patient) bundle.getEntryFirstRep().getResource();
    }

    // Métodos para trabajar con AuditEvent para guardar y verificar códigos
    // ...
}
