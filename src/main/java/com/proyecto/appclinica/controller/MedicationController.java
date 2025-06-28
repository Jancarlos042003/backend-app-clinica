package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.treatment.medication.MedicationResponseDto;
import com.proyecto.appclinica.model.dto.treatment.medication.MedicationStatusUpdateDto;
import com.proyecto.appclinica.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medications")
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping("/today/{identifier}")
    public ResponseEntity<List<MedicationResponseDto>> getMedicationsForToday(@PathVariable String identifier) {
        List<MedicationResponseDto> medications = medicationService.getMedicationsForToday(identifier);
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<List<MedicationResponseDto>> getMedicationsForDate(
            @PathVariable String identifier,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MedicationResponseDto> medications = medicationService.getMedicationsForDate(identifier, date);
        return ResponseEntity.ok(medications);
    }

    @PutMapping("/status")
    public ResponseEntity<MedicationResponseDto> updateMedicationStatus(
            @RequestBody MedicationStatusUpdateDto updateDto) {
        MedicationResponseDto updatedMedication = medicationService.updateMedicationStatus(updateDto);
        return ResponseEntity.ok(updatedMedication);
    }

    @GetMapping("/range/{identifier}")
    public ResponseEntity<List<MedicationResponseDto>> getMedicationsInDateRange(
            @PathVariable String identifier,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MedicationResponseDto> medications = medicationService.getMedicationsInDateRange(identifier, startDate, endDate);
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/range/{identifier}/status")
    public ResponseEntity<List<MedicationResponseDto>> getMedicationsByDateRangeAndStatus(
            @PathVariable String identifier,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String status) {
        List<MedicationResponseDto> medications = medicationService.getMedicationsByDateRangeAndStatus(
                identifier, startDate, endDate, status);
        return ResponseEntity.ok(medications);
    }
}
