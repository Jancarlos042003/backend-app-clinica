package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.treatment.medication.MedicationResponseDto;
import com.proyecto.appclinica.model.dto.treatment.medication.MedicationStatusUpdateDto;

import java.time.LocalDate;
import java.util.List;

public interface MedicationService {

    List<MedicationResponseDto> getMedicationsToday(String identifier);

    List<MedicationResponseDto> getMedicationsByDate(String identifier, LocalDate date);

    MedicationResponseDto updateMedicationStatus(MedicationStatusUpdateDto updateDto);

    void checkPendingMedications();

    List<MedicationResponseDto> getMedicationsInDateRange(String identifier, LocalDate startDate, LocalDate endDate);

    List<MedicationResponseDto> getMedicationsByDateRangeAndStatus(String identifier, LocalDate startDate, LocalDate endDate, String status);
}
