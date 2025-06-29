package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.FhirMedicationStatementException;
import com.proyecto.appclinica.model.entity.MedicationEntity;
import com.proyecto.appclinica.repository.FhirMedicationStatementRepository;
import com.proyecto.appclinica.service.MedicationStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class FhirMedicationStatementServiceImpl implements MedicationStatementService {

    private final FhirMedicationStatementRepository fhirMedicationStatementRepository;

    @Override
    public void createCompletedMedicationStatement(MedicationEntity medication, String patientId) {
        try {
            MedicationStatement medicationStatement = new MedicationStatement();

            // Establecer el estado como "completed"
            medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);

            Reference patientReference = new Reference("Patient/" + patientId);
            medicationStatement.setSubject(patientReference);

            // Establecer el concepto del medicamento
            CodeableConcept medicationConcept = new CodeableConcept();
            medicationConcept.setText(medication.getNameMedicine() + " " +
                    medication.getDoseValue() + " " +
                    medication.getDoseUnit());
            medicationStatement.setMedication(medicationConcept);

            // Establecer la fecha y hora efectiva (cuándo se tomó el medicamento)
            ZonedDateTime effectiveDateTime = ZonedDateTime.ofInstant(
                    medication.getTimeOfTaking().toInstant(),
                    ZoneId.systemDefault());
            DateTimeType effectiveDate = new DateTimeType(Date.from(effectiveDateTime.toInstant()));
            medicationStatement.setEffective(effectiveDate);

            // Establecer la fecha de afirmación (cuándo se registró la toma)
            ZonedDateTime now = ZonedDateTime.now();
            medicationStatement.setDateAsserted(Date.from(now.toInstant()));

            // Establecer que fue tomado
            medicationStatement.setStatus(MedicationStatement.MedicationStatementStatus.COMPLETED);

            // Delegar el guardado al repositorio
            fhirMedicationStatementRepository.saveMedicationStatement(medicationStatement);

        } catch (Exception e) {
            throw new FhirMedicationStatementException("Error al crear el MedicationStatement en FHIR", e);
        }
    }
}
