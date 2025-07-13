package com.proyecto.appclinica.event.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSettingsCreationEvent {
    private String patientId;
}
