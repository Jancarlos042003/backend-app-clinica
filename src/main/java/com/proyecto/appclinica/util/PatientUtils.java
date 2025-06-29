package com.proyecto.appclinica.util;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.proyecto.appclinica.repository.FhirPatientRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para operaciones comunes relacionadas con pacientes
 */
@Slf4j
public class PatientUtils {

    /**
     * Obtiene el ID del paciente a partir del contexto de seguridad actual
     *
     * @param fhirPatientRepository Repositorio de pacientes FHIR
     * @return ID del paciente o "unknown" si no se encuentra
     */
    public static String getPatientId(FhirPatientRepository fhirPatientRepository) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("No hay autenticación en el contexto de seguridad");
            return "unknown";
        }

        String identifier = auth.getName();
        return getPatientIdForIdentifier(identifier, fhirPatientRepository);
    }

    /**
     * Obtiene el ID del paciente a partir de un identificador
     *
     * @param identifier Identificador del paciente
     * @param fhirPatientRepository Repositorio de pacientes FHIR
     * @return ID del paciente o "unknown" si no se encuentra
     */
    public static String getPatientIdForIdentifier(String identifier, FhirPatientRepository fhirPatientRepository) {
        if (identifier == null || identifier.isEmpty()) {
            log.warn("Identificador de paciente vacío");
            return "unknown";
        }

        Patient patient = fhirPatientRepository.getPatientByIdentifier(identifier);
        if (patient == null) {
            log.warn("No se encontró el paciente con el identificador: {}", identifier);
            throw new ResourceNotFoundException("Paciente", "DNI", identifier);
        }

        String patientId = patient.getIdElement().getIdPart();
        log.info("ID del paciente obtenido: {}", patientId);
        return patientId;
    }
}
