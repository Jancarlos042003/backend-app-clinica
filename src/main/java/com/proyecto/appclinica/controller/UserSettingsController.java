package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.EmergencyContactCreateDTO;
import com.proyecto.appclinica.model.dto.EmergencyContactResponseDTO;
import com.proyecto.appclinica.model.dto.MedicationSettingsDTO;
import com.proyecto.appclinica.model.dto.UserSettingsResponseDTO;
import com.proyecto.appclinica.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{patientId}/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping
    public ResponseEntity<UserSettingsResponseDTO> getUserSettings(@PathVariable String patientId) {
        return ResponseEntity.ok(userSettingsService.getUserSettingsResponse(patientId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/medications")
    public ResponseEntity<MedicationSettingsDTO> getMedicationSettings(@PathVariable String patientId) {
        return ResponseEntity.ok(userSettingsService.getMedicationSettings(patientId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/medications")
    public ResponseEntity<MedicationSettingsDTO> updateMedicationSettings(
            @PathVariable String patientId,
            @Valid @RequestBody MedicationSettingsDTO medicationSettingsDTO) {
        return ResponseEntity.ok(userSettingsService.updateMedicationSettings(patientId, medicationSettingsDTO));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/emergency-contacts")
    public ResponseEntity<List<EmergencyContactResponseDTO>> getEmergencyContacts(@PathVariable String patientId) {
        return ResponseEntity.ok(userSettingsService.getEmergencyContacts(patientId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/emergency-contacts")
    public ResponseEntity<EmergencyContactResponseDTO> addEmergencyContact(
            @PathVariable String patientId,
            @Valid @RequestBody EmergencyContactCreateDTO contactDTO) {
        EmergencyContactResponseDTO newContact = userSettingsService.addEmergencyContact(patientId, contactDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newContact);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/emergency-contacts/{contactId}")
    public ResponseEntity<EmergencyContactResponseDTO> updateEmergencyContact(
            @PathVariable String patientId,
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactCreateDTO contactDTO) {
        EmergencyContactResponseDTO updatedContact = userSettingsService.updateEmergencyContact(patientId, contactId, contactDTO);
        return ResponseEntity.ok(updatedContact);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @DeleteMapping("/emergency-contacts/{contactId}")
    public ResponseEntity<Void> deleteEmergencyContact(
            @PathVariable String patientId,
            @PathVariable Long contactId) {
        userSettingsService.deleteEmergencyContact(patientId, contactId);
        return ResponseEntity.noContent().build();
    }
}
