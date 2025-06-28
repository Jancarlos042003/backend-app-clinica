package com.proyecto.appclinica.constant;

/**
 * Constantes para las extensiones FHIR relacionadas con tratamientos médicos
 */
public class TreatmentExtensionConstants {
    private TreatmentExtensionConstants() {
        // Constructor privado para evitar la instanciación
    }

    // Extensiones para horarios de medicación
    public static final String IRREGULAR_SCHEDULE_EXTENSION = "http://appmedica.org/fhir/extensions/irregularSchedule";
    public static final String SCHEDULE_PATTERN_EXTENSION = "http://appmedica.org/fhir/extensions/schedulePattern";
    public static final String CUSTOM_TIME_EXTENSION = "http://appmedica.org/fhir/extensions/customTime";

    // Extensión para el seguimiento de dosis tomadas
    public static final String DOSES_TAKEN_EXTENSION = "http://appmedica.org/fhir/extensions/dosesTaken";
}
