package com.proyecto.appclinica.repository;

import com.proyecto.appclinica.model.entity.ESosStatus;
import com.proyecto.appclinica.model.entity.SosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SosRepository extends JpaRepository<SosEntity, Long> {
    List<SosEntity> findByPatientId(String patientId);
    Optional<SosEntity> findById(Long id);

    @Query("SELECT s FROM SosEntity s WHERE s.dateTime BETWEEN :startDate AND :endDate ORDER BY s.dateTime DESC")
    List<SosEntity> findByDateTimeBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    List<SosEntity> findByStatus(ESosStatus status);
}
