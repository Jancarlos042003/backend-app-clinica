package com.proyecto.appclinica.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.appclinica.model.dto.treatment.CreateTreatmentDto;
import com.proyecto.appclinica.model.dto.treatment.UpdateTreatmentDto;
import com.proyecto.appclinica.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/treatments")
@RequiredArgsConstructor
public class TreatmentController {
    private final TreatmentService treatmentService;
    private final FhirContext fhirContext;

    @PostMapping
    public ResponseEntity<String> createTreatment(@Valid @RequestBody CreateTreatmentDto treatmentDTO) {
        MedicationRequest request = treatmentService.createTreatment(treatmentDTO);

        String responseJson = convertMedicationRequestToFormattedJson(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON) // Establece el tipo de contenido como JSON
                .body(responseJson);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<String> updateTreatment(@PathVariable("id") String medicationRequestId,
                                                  @Valid @RequestBody UpdateTreatmentDto treatmentDTO) {
        MedicationRequest request = treatmentService.updateTreatment(medicationRequestId, treatmentDTO);

        String responseJson = convertMedicationRequestToFormattedJson(request);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelTreatment(@PathVariable("id") String medicationRequestId) {
        MedicationRequest request = treatmentService.cancelTreatment(medicationRequestId);

        String responseJson = convertMedicationRequestToFormattedJson(request);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<String> completeTreatment(@PathVariable("id") String medicationRequestId) {
        MedicationRequest request = treatmentService.completeTreatment(medicationRequestId);

        String responseJson = convertMedicationRequestToFormattedJson(request);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson);
    }

    @GetMapping("/patient/{patientId}/all")
    public ResponseEntity<List<Object>> getAllMedicationRequestsByPatientId(@PathVariable("patientId") String patientId) {
        List<MedicationRequest> medicationRequests = treatmentService.getAllMedicationRequestsByPatientId(patientId);

        List<Object> responseObjects = convertMedicationRequestsToJson(medicationRequests);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseObjects);
    }

    @GetMapping("/patient/{patientId}/all/{status}")
    public ResponseEntity<List<Object>> getAllMedicationRequestsByPatientIdAndStatus(
            @PathVariable("patientId") String patientId,
            @PathVariable("status") String status) {
        List<MedicationRequest> medicationRequests = treatmentService.getAllMedicationRequestsByPatientIdAndStatus(patientId, status);

        List<Object> responseObjects = convertMedicationRequestsToJson(medicationRequests);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseObjects);
    }

    // Uso de fhirContext para convertir a JSON estándar FHIR
    private String convertMedicationRequestToFormattedJson(MedicationRequest request) {
        return treatmentService.getFhirContext().newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(request);
    }

    private List<Object> convertMedicationRequestsToJson(List<MedicationRequest> medicationRequests) {
        IParser jsonParser = fhirContext.newJsonParser();

        return medicationRequests.stream()
                .map(req -> {
                    // Convertir a JSON string
                    String json = jsonParser.encodeResourceToString(req);
                    // Convertir a objeto JSON
                    try {
                        return new ObjectMapper().readValue(json, Object.class);
                    } catch (Exception e) {
                        return Map.of("error", "Error parsing resource");
                    }
                })
                .toList();
    }
}
