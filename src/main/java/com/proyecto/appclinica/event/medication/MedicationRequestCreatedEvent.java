package com.proyecto.appclinica.event.medication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hl7.fhir.r4.model.MedicationRequest;

// Creamos una clase POJO
@Getter
@AllArgsConstructor
public class MedicationRequestCreatedEvent {
    private MedicationRequest medicationRequest;
}
