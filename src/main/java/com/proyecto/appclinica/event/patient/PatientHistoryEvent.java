package com.proyecto.appclinica.event.patient;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PatientHistoryEvent {
    String patientId;
}
