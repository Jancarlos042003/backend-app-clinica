package com.proyecto.appclinica.event.patient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hl7.fhir.r4.model.Patient;

@Getter
@AllArgsConstructor
public class PatientCreatedEvent {
    private Patient patient;
}
