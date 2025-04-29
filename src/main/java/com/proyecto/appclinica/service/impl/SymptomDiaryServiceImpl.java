package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.symptom.SymptomDiaryEntryDto;
import com.proyecto.appclinica.model.dto.symptom.SymptomDto;
import com.proyecto.appclinica.repository.FhirObservationRepository;
import com.proyecto.appclinica.service.SymptomDiaryService;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SymptomDiaryServiceImpl implements SymptomDiaryService {

    private final FhirObservationRepository observationRepository;

    // Formatos de fecha/hora
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("HH:mm:ss");

    // URLs para extensiones personalizadas
    private static final String BODY_SITE_URL = "http://example.org/fhir/StructureDefinition/bodySite";
    private static final String ONSET_TIME_URL = "http://example.org/fhir/StructureDefinition/onsetTime";

    @Override
    public String createDiaryEntry(SymptomDiaryEntryDto dto) {
        Observation observation = buildBaseObservation(dto);
        addSymptomComponents(observation, dto);

        return observationRepository.createObservation(observation);
    }

    @Override
    public String updateDiaryEntry(String observationId, SymptomDiaryEntryDto dto) {
        // Leer observación actual del servidor
        Observation observation = observationRepository.getObservationById(observationId);

        // Actualizar fecha y notas
        observation.setEffective(new DateTimeType(dto.getRecordDateTime().format(ISO_DATE_TIME)));
        observation.setNote(Collections.singletonList(new Annotation().setText(dto.getNotes())));

        // Reemplazar componentes de síntomas
        observation.getComponent().clear();
        addSymptomComponents(observation, dto);

        // Guardar cambios
        return observationRepository.updateObservation(observation);
    }

    @Override
    public List<SymptomDiaryEntryDto> getAllPatientSymptomDiaries(String patientId) {
        // Buscar todas las observaciones de tipo "symptom-group" del paciente
        Bundle results = observationRepository.findSymptomObservationsByPatient(patientId);
        return convertBundleToSymptomDiaryEntries(results);
    }

    @Override
    public List<SymptomDiaryEntryDto> getPatientSymptomDiariesByDateRange(String patientId, LocalDate startDate, LocalDate endDate) {
        Bundle results = observationRepository.findSymptomObservationsByPatientAndDateRange(patientId, startDate, endDate);
        return convertBundleToSymptomDiaryEntries(results);
    }

    @Override
    public SymptomDiaryEntryDto getSymptomDiaryById(String observationId) {
        Observation obs = observationRepository.getObservationById(observationId);
        return convertObservationToSymptomDiaryEntry(obs);
    }

    @Override
    public void deleteSymptomDiary(String observationId) {
        observationRepository.getObservationById(observationId);
        observationRepository.deleteObservation(observationId);
    }


    // ==== MÉTODOS PRIVADOS DE APOYO ====

    /**
     * Crea una instancia base de Observation a partir del DTO.
     */
    private Observation buildBaseObservation(SymptomDiaryEntryDto dto) {
        Observation obs = new Observation();
        obs.setStatus(Observation.ObservationStatus.FINAL);

        obs.getCategoryFirstRep()
                .addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                .setCode("symptom")
                .setDisplay("Symptom");

        obs.getCode()
                .addCoding()
                .setSystem("http://loinc.org")
                .setCode("symptom-group")
                .setDisplay("Registro diario de síntomas");

        obs.getSubject().setReference("Patient/" + dto.getPatientId());
        obs.setEffective(new DateTimeType(dto.getRecordDateTime().format(ISO_DATE_TIME)));
        obs.setNote(Collections.singletonList(new Annotation().setText(dto.getNotes())));

        return obs;
    }

    /**
     * Agrega componentes de síntomas (uno por cada entrada).
     */
    private void addSymptomComponents(Observation obs, SymptomDiaryEntryDto dto) {
        String datePart = dto.getRecordDateTime().toLocalDate().toString();

        for (SymptomDto s : dto.getSymptoms()) {
            Observation.ObservationComponentComponent comp = obs.addComponent();

            comp.getCode().setText(s.getDescription())
                    .addCoding()
                    .setSystem("http://snomed.info/sct")
                    .setCode(s.getCode());

            comp.setValue(new StringType("Severidad: " + s.getSeverity()));

            comp.addExtension().setUrl(BODY_SITE_URL).setValue(new StringType(s.getBodySite()));

            String combinedDateTime = datePart + "T" + s.getOnsetTime().format(TIME_ONLY);
            comp.addExtension().setUrl(ONSET_TIME_URL).setValue(new DateTimeType(combinedDateTime));
        }
    }

    /**
     * Convierte un Bundle FHIR en una lista de DTOs.
     */
    private List<SymptomDiaryEntryDto> convertBundleToSymptomDiaryEntries(Bundle bundle) {
        List<SymptomDiaryEntryDto> entries = new ArrayList<>();
        if (bundle == null || bundle.getEntry() == null) return entries;

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Observation obs) {
                entries.add(convertObservationToSymptomDiaryEntry(obs));
            }
        }
        return entries;
    }

    /**
     * Convierte una única Observation FHIR a SymptomDiaryEntryDto.
     */
    private SymptomDiaryEntryDto convertObservationToSymptomDiaryEntry(Observation obs) {
        SymptomDiaryEntryDto dto = new SymptomDiaryEntryDto();
        dto.setPatientId(obs.getSubject().getReference().replace("Patient/", ""));

        if (obs.getEffective() instanceof DateTimeType dateTime) {
            dto.setRecordDateTime(LocalDateTime.parse(dateTime.getValueAsString(), ISO_DATE_TIME));
        }

        dto.setNotes(obs.hasNote() ? obs.getNoteFirstRep().getText() : "");

        List<SymptomDto> symptoms = new ArrayList<>();
        for (Observation.ObservationComponentComponent component : obs.getComponent()) {
            SymptomDto symptomDto = new SymptomDto();

            if (component.getCode().hasCoding()) {
                symptomDto.setCode(component.getCode().getCodingFirstRep().getCode());
            }

            symptomDto.setDescription(component.getCode().getText());

            if (component.getValue() instanceof StringType valueStr) {
                String severityValue = valueStr.getValue().replace("Severidad: ", "");
                try {
                    symptomDto.setSeverity(Integer.parseInt(severityValue));
                } catch (NumberFormatException e) {
                    symptomDto.setSeverity(0);
                }
            }

            component.getExtension().stream()
                    .filter(ext -> ext.getUrl().equals(BODY_SITE_URL))
                    .findFirst()
                    .ifPresent(ext -> {
                        if (ext.getValue() instanceof StringType val) {
                            symptomDto.setBodySite(val.getValue());
                        }
                    });

            component.getExtension().stream()
                    .filter(ext -> ext.getUrl().equals(ONSET_TIME_URL))
                    .findFirst()
                    .ifPresent(ext -> {
                        if (ext.getValue() instanceof DateTimeType dt) {
                            symptomDto.setOnsetTime(LocalTime.parse(dt.getValueAsString().split("T")[1].substring(0, 8)));
                        }
                    });

            symptoms.add(symptomDto);
        }

        dto.setSymptoms(symptoms);
        return dto;
    }
}