package com.proyecto.appclinica.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "medication_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tolerancia en minutos para tomar medicina (entre 1 y 60 minutos, por defecto 30)
    @NotNull
    @Min(1)
    @Max(60)
    @Builder.Default // Para establecer un valor por defecto. Sin esta anotación, el valor por defecto se ignora.
    private Integer toleranceWindowMinutes = 30;

    // Intervalo de recordatorio en minutos si no ha tomado la medicina (por defecto 10 minutos)
    @NotNull
    @Min(1)
    @Builder.Default
    private Integer reminderFrequencyMinutes = 10;

    // Número máximo de veces que se recordará (por defecto 3, máximo 5)
    @NotNull
    @Min(1)
    @Max(5)
    @Builder.Default
    private Integer maxReminderAttempts = 3;
}
