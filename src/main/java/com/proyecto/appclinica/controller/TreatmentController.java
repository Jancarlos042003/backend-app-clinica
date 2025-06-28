package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.treatment.CreateTreatmentDto;
import com.proyecto.appclinica.model.dto.treatment.TreatmentRecordDto;
import com.proyecto.appclinica.model.dto.treatment.TreatmentResultDto;
import com.proyecto.appclinica.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
@RequiredArgsConstructor
public class TreatmentController {
    private final TreatmentService treatmentService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<TreatmentRecordDto> createTreatment(@Valid @RequestBody CreateTreatmentDto treatmentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(treatmentService.createTreatment(treatmentDTO));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}/update")
    public ResponseEntity<TreatmentRecordDto> updateTreatment(@PathVariable("id") String medicationRequestId,
                                                              @Valid @RequestBody CreateTreatmentDto treatmentDTO) {
        return ResponseEntity.ok(treatmentService.updateTreatment(medicationRequestId, treatmentDTO));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<TreatmentResultDto> cancelTreatment(@PathVariable("id") String medicationRequestId) {
        return ResponseEntity.ok(treatmentService.cancelTreatment(medicationRequestId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}/complete")
    public ResponseEntity<TreatmentResultDto> completeTreatment(@PathVariable("id") String medicationRequestId) {
        return ResponseEntity.ok(treatmentService.completeTreatment(medicationRequestId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{identifier}/all")
    public ResponseEntity<List<TreatmentRecordDto>> getAllMedicationRequestsByPatientId(@PathVariable String identifier) {
        return ResponseEntity.ok(treatmentService.getAllMedicationRequestsByPatientId(identifier));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/patient/{identifier}/all/{status}")
    public ResponseEntity<List<TreatmentRecordDto>> getAllMedicationRequestsByPatientIdAndStatus(
            @PathVariable String identifier,
            @PathVariable String status) {
        return ResponseEntity.ok(treatmentService.getAllMedicationRequestsByPatientIdAndStatus(identifier, status));
    }
}
