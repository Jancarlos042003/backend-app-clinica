package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.entity.MedicationEntity;

public interface MedicationStatementService {

    void createCompletedMedicationStatement(MedicationEntity medication, String patientId);
}
