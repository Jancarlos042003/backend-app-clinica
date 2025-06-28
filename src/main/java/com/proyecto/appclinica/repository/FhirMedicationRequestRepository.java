package com.proyecto.appclinica.repository;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.proyecto.appclinica.exception.FhirClientException;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FhirMedicationRequestRepository {

    private final IGenericClient fhirClient;

    public MedicationRequest saveMedicationRequest(MedicationRequest medicationRequest) {
        MethodOutcome outcome = fhirClient.create()
                .resource(medicationRequest)
                .execute();

        // Devuelve el recurso con ID asignado por el servidor
        MedicationRequest savedRequest;
        if (outcome.getResource() != null) {
            savedRequest = (MedicationRequest) outcome.getResource();
        } else {
            // Si el servidor no devuelve el recurso completo, obtenemos el ID y lo recuperamos
            IdType id = (IdType) outcome.getId();
            savedRequest = fhirClient.read()
                    .resource(MedicationRequest.class)
                    .withId(id)
                    .execute();
        }

        return savedRequest;
    }

    public MedicationRequest getMedicationRequestById(String medicationRequestId) {
        try {
            return fhirClient.read()
                    .resource(MedicationRequest.class)
                    .withId(medicationRequestId)
                    .execute();
        } catch (Exception e) {
            throw new ResourceNotFoundException("MedicationRequest", "ID", medicationRequestId);
        }
    }

    public MedicationRequest updateMedicationRequest(MedicationRequest medicationRequest) {
        MethodOutcome outcome = fhirClient.update()
                .resource(medicationRequest)
                .execute();

        MedicationRequest updatedRequest = (MedicationRequest) outcome.getResource();
        if (updatedRequest == null) {
            // Si el servidor no devuelve el recurso completo, lo recuperamos
            updatedRequest = getMedicationRequestById(medicationRequest.getIdElement().getIdPart());
        }

        return updatedRequest;
    }

    public List<MedicationRequest> findMedicationRequestsByPatientId(String patientId) {
        Bundle bundle = searchFhirBundle(patientId, null);
        return extractMedicationRequestsFromBundle(bundle);
    }

    public List<MedicationRequest> findMedicationRequestsByPatientIdAndStatus(String patientId, String status) {
        Bundle bundle = searchFhirBundle(patientId, status);
        return extractMedicationRequestsFromBundle(bundle);
    }

    private Bundle searchFhirBundle(String patientId, String status) {
        try {
            var search = fhirClient.search()
                    .forResource(MedicationRequest.class)
                    .where(MedicationRequest.SUBJECT.hasId(patientId));

            if (status != null) {
                search.where(MedicationRequest.STATUS.exactly().code(status));
            }

            return search.returnBundle(Bundle.class).execute();
        } catch (ResourceNotFoundException e) {
            return new Bundle(); // Bundle vacío
        } catch (Exception e) {
            throw new FhirClientException("Error en la búsqueda FHIR", e);
        }
    }

    private List<MedicationRequest> extractMedicationRequestsFromBundle(Bundle bundle) {
        if (bundle == null || bundle.getEntry() == null) {
            return new ArrayList<>();
        }

        return bundle.getEntry().stream()
                .filter(entry -> entry.getResource() instanceof MedicationRequest)
                .map(entry -> (MedicationRequest) entry.getResource())
                .toList();
    }
}