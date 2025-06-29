package com.proyecto.appclinica.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationSettingsDTO {

    @NotNull(message = "La ventana de tolerancia no puede ser nula")
    @Min(value = 1, message = "La ventana de tolerancia debe ser al menos 1 minuto")
    private Integer toleranceWindowMinutes;

    @NotNull(message = "La frecuencia de recordatorio no puede ser nula")
    @Min(value = 1, message = "La frecuencia de recordatorio debe ser al menos 1 minuto")
    private Integer reminderFrequencyMinutes;

    @NotNull(message = "El número máximo de intentos de recordatorio no puede ser nulo")
    @Min(value = 1, message = "El número máximo de intentos de recordatorio debe ser al menos 1")
    private Integer maxReminderAttempts;
}
