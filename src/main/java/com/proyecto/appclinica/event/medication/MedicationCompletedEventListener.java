package com.proyecto.appclinica.event.medication;

import com.proyecto.appclinica.model.entity.MedicationEntity;
import com.proyecto.appclinica.service.MedicationStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationCompletedEventListener {

    private final MedicationStatementService fhirMedicationStatementService;

    @EventListener
    @Async
    public void handleMedicationCompletedEvent(MedicationCompletedEvent event) {
        MedicationEntity medication = event.getMedication();
        String patientId = event.getPatientId();

        // Crear el recurso MedicationStatement en FHIR
        fhirMedicationStatementService.createCompletedMedicationStatement(medication, patientId);
    }
}
