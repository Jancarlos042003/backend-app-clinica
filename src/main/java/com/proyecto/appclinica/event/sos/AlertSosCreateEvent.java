package com.proyecto.appclinica.event.sos;

import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.model.entity.SosEntity;
import com.proyecto.appclinica.model.entity.UserSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlertSosCreateEvent {
    private PatientEntity patient;
    private SosEntity sos;
    private UserSettings userSettings;
}
