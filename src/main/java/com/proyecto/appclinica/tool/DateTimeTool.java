package com.proyecto.appclinica.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeTool {

    @Tool(name = "get_current_date_time", description = "Retorna la fecha y hora actual en formato ISO 8601")
    public String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
}
