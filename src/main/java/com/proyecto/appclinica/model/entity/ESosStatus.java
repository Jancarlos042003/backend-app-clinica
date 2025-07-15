package com.proyecto.appclinica.model.entity;

import lombok.Getter;

@Getter
public enum ESosStatus {
    PENDING("pendiente"),
    IN_PROGRESS("en_proceso"),
    RESOLVED("resuelto");

    private final String status;

    ESosStatus(String status) {
        this.status = status;
    }
}
