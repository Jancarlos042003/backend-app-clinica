package com.proyecto.appclinica.event.medication;

import com.proyecto.appclinica.model.entity.MedicationEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MedicationReminderEvent extends ApplicationEvent {
    private final String patientId;
    private final int attemptNumber;

    public MedicationReminderEvent(MedicationEntity medication, String patientId, int attemptNumber) {
        super(medication);
        this.patientId = patientId;
        this.attemptNumber = attemptNumber;
    }

    public MedicationEntity getMedication() {
        return (MedicationEntity) getSource();
    }
}
