package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.PatientProfileResponse;
import com.proyecto.appclinica.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService userService;

    @GetMapping("/identifier")
    public ResponseEntity<PatientProfileResponse> getUserProfile(@RequestParam String identifier) {
        return ResponseEntity.ok(userService.getPatient(identifier));
    }
}
