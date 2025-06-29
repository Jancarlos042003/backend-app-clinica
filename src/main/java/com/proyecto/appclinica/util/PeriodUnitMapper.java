package com.proyecto.appclinica.util;

import org.hl7.fhir.r4.model.Timing;

import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PeriodUnitMapper {

    private PeriodUnitMapper() {
        // Constructor privado para evitar instanciación
    }

    private static final Map<String, Timing.UnitsOfTime> PERIOD_UNIT_MAP;

    static {
        Map<String, Timing.UnitsOfTime> map = new HashMap<>();
        map.put("hour", Timing.UnitsOfTime.H);
        map.put("h", Timing.UnitsOfTime.H);
        map.put("hora", Timing.UnitsOfTime.H);
        map.put("horas", Timing.UnitsOfTime.H);
        map.put("day", Timing.UnitsOfTime.D);
        map.put("d", Timing.UnitsOfTime.D);
        map.put("dia", Timing.UnitsOfTime.D);
        map.put("dias", Timing.UnitsOfTime.D);
        map.put("week", Timing.UnitsOfTime.WK);
        map.put("w", Timing.UnitsOfTime.WK);
        map.put("semana", Timing.UnitsOfTime.WK);
        map.put("semanas", Timing.UnitsOfTime.WK);
        map.put("month", Timing.UnitsOfTime.MO);
        map.put("m", Timing.UnitsOfTime.MO);
        map.put("mes", Timing.UnitsOfTime.MO);
        map.put("meses", Timing.UnitsOfTime.MO);

        PERIOD_UNIT_MAP = Collections.unmodifiableMap(map); // ← Esto lo hace inmutable
    }

    public static Timing.UnitsOfTime map(String input) {
        if (input == null) return Timing.UnitsOfTime.H;

        // Normalizar la entrada para manejar mayúsculas, minúsculas y caracteres especiales
        String normalized = Normalizer.normalize(input.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        return PERIOD_UNIT_MAP.getOrDefault(normalized, Timing.UnitsOfTime.H);
    }
}
