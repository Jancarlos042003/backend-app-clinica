package com.proyecto.appclinica.constant;


public class FhirConstants {
    // Sistemas de codificación
    public static final String SNOMED_CT = "http://snomed.info/sct";
    public static final String LOINC = "http://loinc.org";
    public static final String OBSERVATION_CATEGORY = "http://terminology.hl7.org/CodeSystem/observation-category";

    // Categorías de observación
    public static final String SYMPTOM_CATEGORY = "survey";
    public static final String VITAL_SIGNS_CATEGORY = "vital-signs";

    // Estados de observación
    public static final String STATUS_FINAL = "final";
    public static final String STATUS_PRELIMINARY = "preliminary";

    // Extensiones para horarios de medicación
    public static final String IRREGULAR_SCHEDULE_EXTENSION = "http://appmedica.org/fhir/extensions/irregularSchedule";
    public static final String SCHEDULE_PATTERN_EXTENSION = "http://appmedica.org/fhir/extensions/schedulePattern";
    public static final String CUSTOM_TIME_EXTENSION = "http://appmedica.org/fhir/extensions/customTime";

    private FhirConstants() {
    } // Prevenir instanciación
}
