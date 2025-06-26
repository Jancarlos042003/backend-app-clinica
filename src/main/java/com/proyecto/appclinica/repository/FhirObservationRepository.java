package com.proyecto.appclinica.repository;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.symptom.SymptomRecordDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FhirObservationRepository {

    private final IGenericClient fhirClient;

    // Formatos de fecha/hora
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public SymptomRecordDto createObservation(Observation observation) {
        MethodOutcome outcome = fhirClient.create()
                .resource(observation)
                .execute();

        // Recuperamos la observación creada
        String observationId = outcome.getId().getIdPart();
        Observation createdObs = getObservationById(observationId);

        // Convertimos a DTO
        return convertObservationToSymptomRecord(createdObs);
    }

    public SymptomRecordDto updateObservation(Observation observation) {
        MethodOutcome outcome = fhirClient.update()
                .resource(observation)
                .execute();

        // Recuperamos la observación actualizada
        String observationId = outcome.getId().getIdPart();
        Observation updatedObs = getObservationById(observationId);

        // Convertimos a DTO
        return convertObservationToSymptomRecord(updatedObs);
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

    public List<SymptomRecordDto> findSymptomObservationsByPatient(String patientId) {
        Bundle bundle = fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-diary"))
                .returnBundle(Bundle.class)
                .execute();

        return extractObservationsFromBundle(bundle);
    }

    public List<SymptomRecordDto> getTodaySymptomsByPatient(String patientId) {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(ISO_DATE_TIME); // 00:00
        String endOfDay = today.atTime(LocalTime.MAX).format(ISO_DATE_TIME); // 23:59:59.999999999

        log.info("Buscando síntomas para el paciente {} desde {} hasta {}", patientId, startOfDay, endOfDay);

        Bundle bundle = fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-diary"))
                .and(Observation.DATE.afterOrEquals().second(startOfDay))
                .and(Observation.DATE.beforeOrEquals().second(endOfDay))
                .returnBundle(Bundle.class)
                .execute();

        log.info("Número de síntomas encontrados: {}", bundle.getTotal());

        return extractObservationsFromBundle(bundle);
    }

    public List<SymptomRecordDto> findSymptomObservationsByPatientAndDateRange(String patientId, LocalDate startDate, LocalDate endDate) {
        String startDateIso = startDate.atStartOfDay().format(ISO_DATE_TIME);
        String endDateIso = endDate.atTime(LocalTime.MAX).format(ISO_DATE_TIME);

        Bundle bundle = fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-diary"))
                .and(Observation.DATE.afterOrEquals().day(startDateIso))
                .and(Observation.DATE.beforeOrEquals().day(endDateIso))
                .returnBundle(Bundle.class)
                .execute();

        return extractObservationsFromBundle(bundle);
    }

    /**
     * Extrae observaciones individuales de un Bundle FHIR
     */
    private List<SymptomRecordDto> extractObservationsFromBundle(Bundle bundle) {
        List<Observation> observations = new ArrayList<>();

        if (bundle != null && bundle.getEntry() != null) {
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof Observation) {
                    observations.add((Observation) entry.getResource());
                }
            }
        }

        return observations.stream()
                        .map(this::convertObservationToSymptomRecord)
                        .toList();
    }

    /**
     * Convierte una única Observation FHIR a SymptomRecordDto.
     */
    private SymptomRecordDto convertObservationToSymptomRecord(Observation obs) {
        SymptomRecordDto dto = new SymptomRecordDto();

        // Establecer el ID
        dto.setId(obs.getIdElement().getIdPart());

        log.info("La fecha del síntoma es: {}", obs.getEffective());

        // Establecer la fecha formateada
        if (obs.getEffective() instanceof DateType dateType) {
            LocalDate localDate = dateType.getValue().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            dto.setDate(localDate.format(DISPLAY_DATE_FORMAT));
        } else if (obs.hasEffectiveDateTimeType()) {
            // Manejo de fechas en formato DateTimeType
            LocalDate localDate = obs.getEffectiveDateTimeType().getValueAsCalendar()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            dto.setDate(localDate.format(DISPLAY_DATE_FORMAT));
        } else {
            dto.setDate(LocalDate.now().format(DISPLAY_DATE_FORMAT));
        }

        // Establecer notas si existen
        dto.setNotes(obs.hasNote() ? obs.getNoteFirstRep().getText() : "");

        // Procesar componentes (en este caso solo debe haber uno)
        if (!obs.getComponent().isEmpty()) {
            Observation.ObservationComponentComponent component = obs.getComponentFirstRep();

            // Extraer el nombre del síntoma
            dto.setSymptom(component.getCode().getText());

            // Extraer la intensidad del síntoma
            dto.setIntensity(component.getValueCodeableConcept().getText());
        }

        return dto;
    }
}
