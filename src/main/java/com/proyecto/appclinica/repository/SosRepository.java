package com.proyecto.appclinica.repository;

import com.proyecto.appclinica.model.entity.SosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SosRepository extends JpaRepository<SosEntity, Long> {
    List<SosEntity> findByPatientId(String patientId);
    Optional<SosEntity> findById(Long id);
}
