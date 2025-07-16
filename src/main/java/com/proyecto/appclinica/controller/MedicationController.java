package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.treatment.medication.MedicationResponseDto;
import com.proyecto.appclinica.model.dto.treatment.medication.MedicationStatusUpdateDto;
import com.proyecto.appclinica.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationService medicationService;

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/today/{patientId}")
    public ResponseEntity<List<MedicationResponseDto>> getMedicationsForToday(@PathVariable String patientId) {
        List<MedicationResponseDto> medications = medicationService.getMedicationsToday(patientId);
        return ResponseEntity.ok(medications);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{patientId}")
    public ResponseEntity<List<MedicationResponseDto>> getMedicationsForDate(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MedicationResponseDto> medications = medicationService.getMedicationsByDate(patientId, date);
        return ResponseEntity.ok(medications);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/status")
    public ResponseEntity<MedicationResponseDto> updateMedicationStatus(
            @RequestBody MedicationStatusUpdateDto updateDto) {
        MedicationResponseDto updatedMedication = medicationService.updateMedicationStatus(updateDto);
        return ResponseEntity.ok(updatedMedication);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/range/{patientId}")
    public ResponseEntity<List<MedicationResponseDto>> getMedicationsInDateRange(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MedicationResponseDto> medications = medicationService.getMedicationsInDateRange(patientId, startDate, endDate);
        return ResponseEntity.ok(medications);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/range/{patientId}/status")
    public ResponseEntity<List<MedicationResponseDto>> getMedicationsByDateRangeAndStatus(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String status) {
        List<MedicationResponseDto> medications = medicationService.getMedicationsByDateRangeAndStatus(
                patientId, startDate, endDate, status);
        return ResponseEntity.ok(medications);
    }
}
