package com.proyecto.appclinica.repository;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Repository
@RequiredArgsConstructor
public class FhirObservationRepository {

    private final IGenericClient fhirClient;

    // Formatos de fecha/hora
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;

    public String createObservation(Observation observation) {
        MethodOutcome outcome = fhirClient.create()
                .resource(observation)
                .execute();
        return outcome.getId().getValue();
    }

    public String updateObservation(Observation observation) {
        MethodOutcome outcome = fhirClient.update()
                .resource(observation)
                .execute();
        return outcome.getId().getValue();
    }

    public Observation getObservationById(String observationId) {
        try {
            return fhirClient.read()
                    .resource(Observation.class)
                    .withId(observationId)
                    .execute();
        } catch (ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Observación", "ID", observationId);
        }
    }


    public void deleteObservation(String observationId) {
        fhirClient.delete()
                .resourceById("Observation", observationId)
                .execute();
    }

    public Bundle findSymptomObservationsByPatient(String patientId) {
        return fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-group"))
                .returnBundle(Bundle.class)
                .execute();
    }

    public Bundle findSymptomObservationsByPatientAndDateRange(String patientId, LocalDate startDate, LocalDate endDate) {
        String startDateIso = startDate.atStartOfDay().format(ISO_DATE_TIME);
        String endDateIso = endDate.atTime(LocalTime.MAX).format(ISO_DATE_TIME);

        return fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-group"))
                .and(Observation.DATE.afterOrEquals().day(startDateIso))
                .and(Observation.DATE.beforeOrEquals().day(endDateIso))
                .returnBundle(Bundle.class)
                .execute();
    }
}
