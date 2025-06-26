package com.proyecto.appclinica.constant;

import com.proyecto.appclinica.util.IntensityCode;

import java.util.HashMap;
import java.util.Map;

public class IntensityCodes {
    private static final Map<String, IntensityCode> INTENSITY_CODES = new HashMap<>();

    static {
        INTENSITY_CODES.put("Leve", new IntensityCode("255604002", "Mild"));
        INTENSITY_CODES.put("Moderada", new IntensityCode("6736007", "Moderate"));
        INTENSITY_CODES.put("Severa", new IntensityCode("24484000", "Severe"));
    }

    /**
     * Obtiene el mapa de códigos de intensidad
     * @return Mapa con los códigos de intensidad
     */
    public static Map<String, IntensityCode> getIntensityCodes() {
        return INTENSITY_CODES;
    }

    /**
     * Obtiene un código de intensidad específico
     * @param key La clave del código de intensidad (Leve, Moderada, Severa)
     * @return El código de intensidad correspondiente
     */
    public static IntensityCode getIntensityCode(String key) {
        return INTENSITY_CODES.get(key);
    }

    /**
     * Sistema SNOMED CT para intensidades de síntomas
     */
    public static final String INTENSITY_SYSTEM = "http://snomed.info/sct";
}
