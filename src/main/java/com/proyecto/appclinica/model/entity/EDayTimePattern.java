package com.proyecto.appclinica.model.entity;

import java.time.LocalTime;

public enum EDayTimePattern {
    MORNING(LocalTime.of(8, 0)),
    NOON(LocalTime.of(12, 0)),
    AFTERNOON(LocalTime.of(16, 0)),
    EVENING(LocalTime.of(20, 0)),
    NIGHT(LocalTime.of(23, 0));

    private final LocalTime defaultTime;

    EDayTimePattern(LocalTime defaultTime) {
        this.defaultTime = defaultTime;
    }

    public LocalTime getDefaultTime() {
        return defaultTime;
    }
}
