package com.proyecto.appclinica.model.dto.treatment.medication;

public record MedicationResponseDto(
        Long medicationId,
        String nameMedicine,
        String doses, // e.g., "2 mg", "1 tablet"
        String timeOfTaking, // e.g., "08:15", "12:30", "16:00", "20:45", "23:00"
        String status // e.g., "Activo", "Completado", "No tomado", "Introducido en error", "Intencionado", "Detenido", "En espera", "Desconocido"
) {
}
