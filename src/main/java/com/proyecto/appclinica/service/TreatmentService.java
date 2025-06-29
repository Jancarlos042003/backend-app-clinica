package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.treatment.CreateTreatmentDto;
import com.proyecto.appclinica.model.dto.treatment.TreatmentResultDto;
import com.proyecto.appclinica.model.dto.treatment.TreatmentRecordDto;

import java.util.List;

public interface TreatmentService {
    TreatmentRecordDto createTreatment(CreateTreatmentDto treatmentDTO);

    TreatmentRecordDto updateTreatment(String medicationRequestId, CreateTreatmentDto treatmentDTO);

    TreatmentResultDto cancelTreatment(String medicationRequestId);

    TreatmentResultDto completeTreatment(String medicationRequestId);

    List<TreatmentRecordDto> getAllMedicationRequestsByPatientId(String identifier);

    List<TreatmentRecordDto> getAllMedicationRequestsByPatientIdAndStatus(String identifier, String status);
}
