package com.proyecto.appclinica.repository;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FhirMedicationStatementRepository {

    private final IGenericClient fhirClient;

    public MedicationStatement saveMedicationStatement(MedicationStatement medicationStatement) {
        try {
            // Guardar el recurso en el servidor FHIR
            MethodOutcome outcome = fhirClient.create()
                    .resource(medicationStatement)
                    .execute();

            // Obtener el recurso guardado con su ID
            if (outcome.getResource() != null) {
                log.info("MedicationStatement creado con ID: {}", outcome.getId().getValue());
                return (MedicationStatement) outcome.getResource();
            } else {
                IdType id = new IdType(outcome.getId().getValue());
                log.info("MedicationStatement creado con ID: {}", id.getValue());

                return fhirClient.read()
                        .resource(MedicationStatement.class)
                        .withId(id)
                        .execute();
            }
        } catch (Exception e) {
            log.error("Error al crear el MedicationStatement: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar el MedicationStatement en FHIR", e);
        }
    }
}
