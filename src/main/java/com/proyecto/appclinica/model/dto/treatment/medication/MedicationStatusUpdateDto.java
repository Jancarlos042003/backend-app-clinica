package com.proyecto.appclinica.model.dto.treatment.medication;

public record MedicationStatusUpdateDto(
        Long medicationId,
        String status
) {
}
