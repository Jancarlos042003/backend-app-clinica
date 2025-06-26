package com.proyecto.appclinica.constant;

import java.util.HashMap;
import java.util.Map;

public class SymptomCodes {

    private static final Map<String, String> SYMPTOM_MAP = new HashMap<>();

    static {
        // Síntomas comunes con códigos SNOMED CT
        SYMPTOM_MAP.put("dolor de cabeza", "25064002");
        SYMPTOM_MAP.put("cefalea", "25064002");
        SYMPTOM_MAP.put("headache", "25064002");

        SYMPTOM_MAP.put("fiebre", "386661006");
        SYMPTOM_MAP.put("fever", "386661006");
        SYMPTOM_MAP.put("temperatura alta", "386661006");

        SYMPTOM_MAP.put("dolor", "22253000");
        SYMPTOM_MAP.put("pain", "22253000");
        SYMPTOM_MAP.put("molestia", "22253000");

        SYMPTOM_MAP.put("tos", "49727002");
        SYMPTOM_MAP.put("cough", "49727002");

        SYMPTOM_MAP.put("náuseas", "422587007");
        SYMPTOM_MAP.put("nausea", "422587007");
        SYMPTOM_MAP.put("ganas de vomitar", "422587007");

        SYMPTOM_MAP.put("fatiga", "84229001");
        SYMPTOM_MAP.put("cansancio", "84229001");
        SYMPTOM_MAP.put("fatigue", "84229001");

        SYMPTOM_MAP.put("mareo", "404640003");
        SYMPTOM_MAP.put("dizziness", "404640003");
        SYMPTOM_MAP.put("vértigo", "399153001");

        SYMPTOM_MAP.put("dolor abdominal", "21522001");
        SYMPTOM_MAP.put("dolor de estómago", "21522001");
        SYMPTOM_MAP.put("stomach pain", "21522001");
    }

    /**
     * Obtiene el código SNOMED CT para un síntoma
     * @param symptom Nombre del síntoma (case-insensitive)
     * @return Código SNOMED CT o null si no se encuentra
     */
    public static String getCode(String symptom) {
        if (symptom == null) return null;
        return SYMPTOM_MAP.get(symptom.toLowerCase().trim());
    }

    /**
     * Busca códigos que contengan parte del texto del síntoma
     * @param partialSymptom Parte del nombre del síntoma
     * @return Primera coincidencia encontrada o null
     */
    public static String findCodeByPartialMatch(String partialSymptom) {
        if (partialSymptom == null) return null;

        String searchTerm = partialSymptom.toLowerCase().trim();
        return SYMPTOM_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().contains(searchTerm))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtiene todos los síntomas disponibles
     * @return Map con síntomas y códigos
     */
    public static Map<String, String> getAllSymptoms() {
        return new HashMap<>(SYMPTOM_MAP);
    }

    /**
     * Código por defecto para síntomas no clasificados
     */
    public static final String UNSPECIFIED_SYMPTOM_CODE = "418799008"; // Síntoma no especificado

    private SymptomCodes() {} // Prevenir instanciación
}