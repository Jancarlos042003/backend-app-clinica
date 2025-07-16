package com.proyecto.appclinica.repository;

import com.proyecto.appclinica.model.entity.EMedicationStatementStatus;
import com.proyecto.appclinica.model.entity.MedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<MedicationEntity, Long> {

    List<MedicationEntity> findAllByPatientIdAndTimeOfTakingBetween(
            String patientId, Timestamp startTime, Timestamp endTime);

    Optional<MedicationEntity> findById(Long id);
    
    List<MedicationEntity> findByMedicationRequestId(String medicationRequestId);

    List<MedicationEntity> findAllByStatusAndTimeOfTakingBetween(
            EMedicationStatementStatus status, Timestamp startTime, Timestamp endTime
    );

    List<MedicationEntity> findAllByPatientIdAndTimeOfTakingBetweenAndStatus(
            String patientId, Timestamp startTime, Timestamp endTime, EMedicationStatementStatus status
    );
}
