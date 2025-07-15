package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.SosRequest;
import com.proyecto.appclinica.model.dto.SosResponse;
import com.proyecto.appclinica.model.dto.SosUpdateRequest;
import com.proyecto.appclinica.service.SosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sos")
public class SosController {
    
    private final SosService sosService;

    @PostMapping
    public ResponseEntity<SosResponse> createSos(@Valid @RequestBody SosRequest sosRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sosService.createSos(sosRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SosResponse> getSosById(@PathVariable Long id) {
        return ResponseEntity.ok(sosService.getSosById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SosResponse> updateSos(
            @PathVariable Long id, 
            @Valid @RequestBody SosUpdateRequest sosUpdateRequest) {
        return ResponseEntity.ok(sosService.updateSos(id, sosUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSos(@PathVariable Long id) {
        return ResponseEntity.ok(sosService.deleteSos(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<SosResponse>> getSosByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(sosService.getSosByPatientId(patientId));
    }
}