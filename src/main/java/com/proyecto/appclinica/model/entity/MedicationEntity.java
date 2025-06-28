package com.proyecto.appclinica.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "medications")
public class MedicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_medicine", nullable = false)
    private String nameMedicine;

    @Column(name = "dose_value", nullable = false)
    private BigDecimal doseValue;

    @Column(name = "dose_unit", nullable = false)
    private String doseUnit;

    @Column(name = "time_of_taking", nullable = false)
    private Timestamp timeOfTaking;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "medication_request_id")
    private String medicationRequestId;

    @Enumerated(EnumType.STRING)
    private EMedicationStatementStatus status;

    @Column(name = "is_irregular")
    private boolean isIrregular;

    @Column(name = "schedule_pattern")
    private String schedulePattern;
}
