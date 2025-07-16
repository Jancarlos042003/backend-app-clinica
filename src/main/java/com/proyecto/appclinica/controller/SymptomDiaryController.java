package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.symptom.SymptomDto;
import com.proyecto.appclinica.model.dto.symptom.SymptomRecordDto;
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
    public ResponseEntity<SymptomRecordDto> createEntry(@Valid @RequestBody SymptomDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(symptomDiaryService.createDiaryEntry(dto));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}")
    public ResponseEntity<SymptomRecordDto> updateEntry(
            @PathVariable String id,
            @Valid @RequestBody SymptomDto dto) {
        return ResponseEntity.ok(symptomDiaryService.updateDiaryEntry(id, dto));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<SymptomRecordDto>> getAllPatientDiaries(
            @PathVariable String patientId) {
        List<SymptomRecordDto> entries = symptomDiaryService.getAllPatientSymptomDiaries(patientId);
        return ResponseEntity.ok(entries);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<List<SymptomRecordDto>> getPatientDiariesByDateRange(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(symptomDiaryService.getPatientSymptomDiariesByDateRange(patientId, startDate, endDate));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}/todays-symptoms")
    public ResponseEntity<List<SymptomRecordDto>> getTodaySymptomsByPatient(
            @PathVariable String patientId) {
        List<SymptomRecordDto> todaySymptoms = symptomDiaryService.getTodaySymptomsByPatient(patientId);
        return ResponseEntity.ok(todaySymptoms);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{observationId}")
    public ResponseEntity<SymptomRecordDto> getDiaryById(
            @PathVariable String observationId) {
        return ResponseEntity.ok(symptomDiaryService.getSymptomDiaryById(observationId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @DeleteMapping("/{observationId}")
    public ResponseEntity<Void> deleteDiaryEntry(@PathVariable String observationId) {
        symptomDiaryService.deleteSymptomDiary(observationId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}/todays-registered-symptoms")
    public ResponseEntity<List<SymptomRecordDto>> getTodayRegisteredSymptomsByPatient(
            @PathVariable String patientId) {
        List<SymptomRecordDto> todayRegisteredSymptoms = symptomDiaryService.getTodayRegisteredSymptomsByPatient(patientId);
        return ResponseEntity.ok(todayRegisteredSymptoms);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{patientId}/registration-date-range")
    public ResponseEntity<List<SymptomRecordDto>> getPatientDiariesByRegistrationDateRange(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(symptomDiaryService.getPatientSymptomDiariesByRegistrationDateRange(patientId, startDate, endDate));
    }
}
