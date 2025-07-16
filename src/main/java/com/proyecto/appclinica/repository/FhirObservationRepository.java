package com.proyecto.appclinica.repository;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
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

    /**
     * Obtiene síntomas que OCURRIERON hoy (por fecha efectiva del síntoma)
     */
    public List<SymptomRecordDto> getTodaySymptomsByPatient(String patientId) {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(ISO_DATE_TIME);
        String endOfDay = today.atTime(LocalTime.MAX).format(ISO_DATE_TIME);

        log.info("Buscando síntomas que OCURRIERON hoy ({}) para el paciente {}", today, patientId);

        Bundle bundle = fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-diary"))
                .and(Observation.DATE.afterOrEquals().second(startOfDay))
                .and(Observation.DATE.beforeOrEquals().second(endOfDay))
                .returnBundle(Bundle.class)
                .execute();

        log.info("Número de síntomas que ocurrieron hoy: {}", bundle.getTotal());

        return extractObservationsFromBundle(bundle);
    }

    /**
     * Obtiene síntomas REGISTRADOS hoy (por fecha de creación en el sistema)
     */
    public List<SymptomRecordDto> getTodayRegisteredSymptomsByPatient(String patientId) {
        LocalDate today = LocalDate.now();
        String startOfDay = today.atStartOfDay().format(ISO_DATE_TIME);
        String endOfDay = today.atTime(LocalTime.MAX).format(ISO_DATE_TIME);

        log.info("Buscando síntomas REGISTRADOS hoy ({}) para el paciente {}", today, patientId);

        Bundle bundle = fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-diary"))
                .and(new DateClientParam("_lastUpdated").afterOrEquals().second(startOfDay))
                .and(new DateClientParam("_lastUpdated").beforeOrEquals().second(endOfDay))
                .returnBundle(Bundle.class)
                .execute();

        log.info("Número de síntomas registrados hoy: {}", bundle.getTotal());

        return extractObservationsFromBundle(bundle);
    }

    /**
     * Busca síntomas por fecha de OCURRENCIA (fecha efectiva del síntoma)
     */
    public List<SymptomRecordDto> findSymptomObservationsByPatientAndDateRange(String patientId, LocalDate startDate, LocalDate endDate) {
        String startDateIso = startDate.atStartOfDay().format(ISO_DATE_TIME);
        String endDateIso = endDate.atTime(LocalTime.MAX).format(ISO_DATE_TIME);

        log.info("Buscando síntomas que OCURRIERON entre {} y {} para paciente {}", startDate, endDate, patientId);

        Bundle bundle = fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-diary"))
                .and(Observation.DATE.afterOrEquals().day(startDateIso))
                .and(Observation.DATE.beforeOrEquals().day(endDateIso))
                .returnBundle(Bundle.class)
                .execute();

        log.info("Número de síntomas encontrados por fecha de ocurrencia: {}", bundle.getTotal());

        return extractObservationsFromBundle(bundle);
    }

    /**
     * Busca síntomas por fecha de REGISTRO (fecha de creación en el sistema)
     */
    public List<SymptomRecordDto> findSymptomObservationsByPatientAndRegistrationDateRange(String patientId, LocalDate startDate, LocalDate endDate) {
        String startDateIso = startDate.atStartOfDay().format(ISO_DATE_TIME);
        String endDateIso = endDate.atTime(LocalTime.MAX).format(ISO_DATE_TIME);

        log.info("Buscando síntomas REGISTRADOS entre {} y {} para paciente {}", startDate, endDate, patientId);

        Bundle bundle = fhirClient.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("subject").hasId("Patient/" + patientId))
                .and(new TokenClientParam("code").exactly().code("symptom-diary"))
                .and(new DateClientParam("_lastUpdated").afterOrEquals().day(startDateIso))
                .and(new DateClientParam("_lastUpdated").beforeOrEquals().day(endDateIso))
                .returnBundle(Bundle.class)
                .execute();

        log.info("Número de síntomas encontrados por fecha de registro: {}", bundle.getTotal());

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
