package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.symptom.SymptomDiaryEntryDto;
import com.proyecto.appclinica.service.SymptomDiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/symptom-diary")
@RequiredArgsConstructor
public class SymptomDiaryController {
    private final SymptomDiaryService symptomDiaryService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<String> createEntry(@Valid @RequestBody SymptomDiaryEntryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(symptomDiaryService.createDiaryEntry(dto));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateEntry(
            @PathVariable String id,
            @Valid @RequestBody SymptomDiaryEntryDto dto) {
        return ResponseEntity.ok(symptomDiaryService.updateDiaryEntry(id, dto));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<SymptomDiaryEntryDto>> getAllPatientDiaries(
            @PathVariable String patientId) {
        List<SymptomDiaryEntryDto> entries = symptomDiaryService.getAllPatientSymptomDiaries(patientId);
        return ResponseEntity.ok(entries);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<List<SymptomDiaryEntryDto>> getPatientDiariesByDateRange(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SymptomDiaryEntryDto> entries = symptomDiaryService.getPatientSymptomDiariesByDateRange(
                patientId, startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{observationId}")
    public ResponseEntity<SymptomDiaryEntryDto> getDiaryById(
            @PathVariable String observationId) {
        return ResponseEntity.ok(symptomDiaryService.getSymptomDiaryById(observationId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @DeleteMapping("/{observationId}")
    public ResponseEntity<Void> deleteDiaryEntry(@PathVariable String observationId) {
        symptomDiaryService.deleteSymptomDiary(observationId);
        return ResponseEntity.noContent().build();
    }
}
