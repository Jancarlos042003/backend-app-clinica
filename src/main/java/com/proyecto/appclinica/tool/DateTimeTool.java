package com.proyecto.appclinica.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class DateTimeTool {

    @Tool(name = "get_current_date_time", description = "Retorna la fecha y hora actual en formato ISO 8601")
    public String getCurrentDateTime() {
        // Definimos la zona horaria de Perú
        ZoneId zoneId = ZoneId.of("America/Lima");
        // Obtenemos la fecha y hora actual en la zona horaria de Perú
        ZonedDateTime nowInPeru = ZonedDateTime.now(zoneId);
        // Retornamos la fecha y hora formateada
        return nowInPeru.toString();
    }
}
