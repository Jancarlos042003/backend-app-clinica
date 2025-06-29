package com.proyecto.appclinica.repository;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FhirMedicationStatementRepository {

    private final IGenericClient fhirClient;

    public MedicationStatement saveMedicationStatement(MedicationStatement medicationStatement) {
        // Guardar el recurso en el servidor FHIR
        MethodOutcome outcome = fhirClient.create()
                .resource(medicationStatement)
                .execute();

        // Obtener el recurso guardado con su ID
        if (outcome.getResource() != null) {
            return (MedicationStatement) outcome.getResource();
        } else {
            IdType id = new IdType(outcome.getId().getValue());
            return fhirClient.read()
                    .resource(MedicationStatement.class)
                    .withId(id)
                    .execute();
        }
    }
}
