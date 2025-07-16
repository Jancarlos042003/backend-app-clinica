package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.constant.FhirConstants;
import com.proyecto.appclinica.constant.IntensityCodes;
import com.proyecto.appclinica.constant.SymptomCodes;
import com.proyecto.appclinica.exception.InvalidRequestException;
import com.proyecto.appclinica.model.dto.symptom.SymptomDto;
import com.proyecto.appclinica.model.dto.symptom.SymptomRecordDto;
import com.proyecto.appclinica.repository.FhirObservationRepository;
import com.proyecto.appclinica.service.SymptomDiaryService;
import com.proyecto.appclinica.util.IntensityCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomDiaryServiceImpl implements SymptomDiaryService {

    private final FhirObservationRepository observationRepository;

    // URLs para extensiones personalizadas
    private static final String DURATION_URL = "http://example.org/fhir/StructureDefinition/duration";

    @Override
    public SymptomRecordDto createDiaryEntry(SymptomDto dto) {
        // Crea la observación con los datos del síntoma
        Observation observation = buildBaseObservation(dto);
        addSymptomComponent(observation, dto);

        return observationRepository.createObservation(observation);
    }

    @Override
    public SymptomRecordDto updateDiaryEntry(String observationId, SymptomDto dto) {
        // Leer observación actual del servidor
        Observation observation = observationRepository.getObservationById(observationId);

        // Actualizar fecha y notas
        observation.setEffective(new DateTimeType(dto.getDate()));

        if (dto.getNotes() != null && !dto.getNotes().isEmpty()) {
            observation.setNote(Collections.singletonList(new Annotation().setText(dto.getNotes())));
        } else {
            observation.setNote(new ArrayList<>());
        }

        // Reemplazar componentes de síntomas
        observation.getComponent().clear();
        addSymptomComponent(observation, dto);

        // Guardar cambios
        return observationRepository.updateObservation(observation);
    }

    @Override
    public List<SymptomRecordDto> getAllPatientSymptomDiaries(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("El ID del paciente no puede estar vacío o nulo.");
        }

        return observationRepository.findSymptomObservationsByPatient(patientId);
    }

    @Override
    public List<SymptomRecordDto> getPatientSymptomDiariesByDateRange(String patientId, LocalDate startDate, LocalDate endDate) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("El ID del paciente no puede estar vacío o nulo.");
        }

        return observationRepository.findSymptomObservationsByPatientAndDateRange(patientId, startDate, endDate);
    }

    @Override
    public List<SymptomRecordDto> getTodaySymptomsByPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("El ID del paciente no puede estar vacío o nulo.");
        }

        return observationRepository.getTodaySymptomsByPatient(patientId);
    }

    @Override
    public SymptomRecordDto getSymptomDiaryById(String observationId) {
        Observation obs = observationRepository.getObservationById(observationId);
        return convertObservationToSymptomRecord(obs);
    }

    @Override
    public void deleteSymptomDiary(String observationId) {
        observationRepository.getObservationById(observationId); // Verifica que exista
        observationRepository.deleteObservation(observationId);
    }

    @Override
    public List<SymptomRecordDto> getTodayRegisteredSymptomsByPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("El ID del paciente no puede estar vacío o nulo.");
        }

        return observationRepository.getTodayRegisteredSymptomsByPatient(patientId);
    }

    @Override
    public List<SymptomRecordDto> getPatientSymptomDiariesByRegistrationDateRange(String patientId, LocalDate startDate, LocalDate endDate) {
        if (patientId == null || patientId.isEmpty()) {
            throw new InvalidRequestException("El ID del paciente no puede estar vacío o nulo.");
        }

        return observationRepository.findSymptomObservationsByPatientAndRegistrationDateRange(patientId, startDate, endDate);
    }


    // ==== MÉTODOS PRIVADOS DE APOYO ====

    /**
     * Crea una instancia base de Observation a partir del DTO.
     */
    private Observation buildBaseObservation(SymptomDto dto) {
        Observation obs = new Observation();
        obs.setStatus(Observation.ObservationStatus.FINAL);

        // Agregar una categoría a la observación
        obs.getCategoryFirstRep().addCoding().setSystem(FhirConstants.OBSERVATION_CATEGORY).setCode(FhirConstants.SYMPTOM_CATEGORY).setDisplay("Symptom");

        // Establecer el código de la observación
        obs.getCode().addCoding().setSystem(FhirConstants.LOINC).setCode("symptom-diary").setDisplay("Registro de síntoma");

        // Establecer al paciente como sujeto de la observación
        obs.getSubject().setReference("Patient/" + dto.getPatientId());

        // Establecer la fecha de efectividad
        obs.setEffective(new DateTimeType(dto.getDate()));

        if (dto.getNotes() != null && !dto.getNotes().isEmpty()) {
            obs.setNote(Collections.singletonList(new Annotation().setText(dto.getNotes())));
        }

        return obs;
    }

    /**
     * Agrega componente de síntoma a la observación.
     */
    private void addSymptomComponent(Observation obs, SymptomDto dto) {
        Observation.ObservationComponentComponent comp = obs.addComponent();

        // Establecer el código/nombre del síntoma
        String symptomCode = SymptomCodes.getCode(dto.getSymptom());
        if (symptomCode == null) {
            // Si no hay coincidencia exacta, intentar encontrar coincidencia parcial
            symptomCode = SymptomCodes.findCodeByPartialMatch(dto.getSymptom());
            if (symptomCode == null) {
                // Si todavía no hay coincidencia, usar código de síntoma no especificado
                symptomCode = SymptomCodes.UNSPECIFIED_SYMPTOM_CODE;
            }
        }

        comp.getCode().setText(dto.getSymptom()).addCoding().setSystem(FhirConstants.SNOMED_CT).setCode(symptomCode).setDisplay(dto.getSymptom());

        // Crear CodeableConcept para la intensidad
        CodeableConcept intensityCodeableConcept = new CodeableConcept();
        String intensityValue = dto.getIntensity();

        // Establecer el código SNOMED CT y display en inglés para la intensidad
        IntensityCode intensityCode = IntensityCodes.getIntensityCode(intensityValue);

        if (intensityCode != null) {
            // Si corresponde a una intensidad estándar conocida
            intensityCodeableConcept.addCoding().setSystem(IntensityCodes.INTENSITY_SYSTEM).setCode(intensityCode.getCode()).setDisplay(intensityCode.getDescription());
        } else {
            // Si no es un valor estándar, usar un código genérico para "No especificado"
            // y almacenar el valor original como texto
            intensityCodeableConcept.addCoding().setSystem(IntensityCodes.INTENSITY_SYSTEM).setCode("260395002")  // Código SNOMED CT para "No especificado"
                    .setDisplay("Not specified");
        }

        // Guardar el valor original en español como texto para referencia
        intensityCodeableConcept.setText(intensityValue);
        comp.setValue(intensityCodeableConcept);

        // Agregar duración como extensión si está presente
        if (dto.getDuration() != null) {
            comp.addExtension().setUrl(DURATION_URL).setValue(new IntegerType(dto.getDuration()));
        }
    }

    /**
     * Convierte una única Observation FHIR a SymptomRecordDto.
     */
    private SymptomRecordDto convertObservationToSymptomRecord(Observation obs) {
        SymptomRecordDto dto = new SymptomRecordDto();

        // Establecer el ID
        dto.setId(obs.getIdElement().getIdPart());

        // Establecer la fecha formateada
        if (obs.getEffective() instanceof DateType dateType) {
            LocalDate localDate = dateType.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            dto.setDate(localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            dto.setDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        // Establecer notas si existen
        dto.setNotes(obs.hasNote() ? obs.getNoteFirstRep().getText() : "");

        // Procesar componentes (en este caso solo debe haber uno)
        if (!obs.getComponent().isEmpty()) {
            Observation.ObservationComponentComponent component = obs.getComponentFirstRep();

            // Extraer el nombre del síntoma
            dto.setSymptom(component.getCode().getText());

            // Extraer la intensidad según el tipo de valor
            if (component.getValue() instanceof CodeableConcept codeableConcept) {
                // Si es un CodeableConcept (nueva forma de almacenar intensidad con SNOMED CT)
                if (codeableConcept.hasText()) {
                    // Si tiene un texto directo (el valor en español), usarlo directamente
                    dto.setIntensity(codeableConcept.getText());
                } else {
                    // Intentar recuperar el código SNOMED CT y mapearlo a valor en español
                    for (Coding coding : codeableConcept.getCoding()) {
                        if (IntensityCodes.INTENSITY_SYSTEM.equals(coding.getSystem())) {
                            // Buscar el valor en español basado en el código
                            String codeSnomedCt = coding.getCode();

                            // Buscar en el mapa de intensidades comparando por código
                            for (Map.Entry<String, com.proyecto.appclinica.util.IntensityCode> entry : IntensityCodes.getIntensityCodes().entrySet()) {

                                if (entry.getValue().getCode().equals(codeSnomedCt)) {
                                    // Encontramos coincidencia, usar la clave en español
                                    dto.setIntensity(entry.getKey());
                                    return dto;
                                }
                            }

                            // Si no encuentra mapeo, usar el valor de visualización (en inglés)
                            dto.setIntensity(coding.getDisplay());
                            return dto;
                        }
                    }
                    // Si no encontró sistema SNOMED, usar cualquier texto disponible
                    dto.setIntensity(codeableConcept.getCodingFirstRep().getDisplay());
                }
            } else if (component.getValue() instanceof StringType valueString) {
                dto.setIntensity(valueString.getValue());
            } else if (component.getValue() instanceof IntegerType valueInt) {
                // Para compatibilidad con registros existentes que usen IntegerType
                dto.setIntensity(String.valueOf(valueInt.getValue()));
            } else {
                dto.setIntensity("");
            }
        }

        return dto;
    }
}
