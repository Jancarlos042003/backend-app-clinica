package com.proyecto.appclinica.event.medication;

import com.proyecto.appclinica.model.entity.MedicationEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Este evento se utiliza para notificar que una medicación ha sido completada
 */

@Getter
public class MedicationCompletedEvent extends ApplicationEvent {
    private final String patientId;

    public MedicationCompletedEvent(MedicationEntity medication, String patientId) {
        super(medication);
        this.patientId = patientId;
    }

    public MedicationEntity getMedication() {
        return (MedicationEntity) getSource();
    }
}
