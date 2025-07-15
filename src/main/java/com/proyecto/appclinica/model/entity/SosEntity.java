package com.proyecto.appclinica.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sos")
public class SosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id")
    @NotNull(message = "El ID del paciente no puede ser nulo")
    private String patientId;

    @NotNull(message = "La latitud no puede ser nula")
    private BigDecimal latitude;

    @NotNull(message = "La longitud no puede ser nula")
    private BigDecimal longitude;

    @NotNull(message = "La dirección no puede ser nula")
    private String address;

    @NotNull(message = "La fecha y hora no pueden ser nulas")
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "patient_message", length = 500)
    private String patientMessage;

    @Column(name = "ai_report", columnDefinition = "TEXT")
    private String aiReport;

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(value = EnumType.STRING)
    private ESosStatus status; // Puede ser "pendiente", "en_proceso", "resuelto"
}
