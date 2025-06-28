package com.proyecto.appclinica.model.entity;

import lombok.Getter;

@Getter
public enum EMedicationStatementStatus {
    ACTIVE("Activo"),
    COMPLETED("Completado"),
    NOT_TAKEN("No tomado"),
    ENTERED_IN_ERROR("Introducido en error"),
    INTENDED("Por tomar"),
    STOPPED("Detenido"),
    ON_HOLD("En espera"),
    UNKNOWN("Desconocido");

    private final String description;

    EMedicationStatementStatus(String description) {
        this.description = description;
    }

}
