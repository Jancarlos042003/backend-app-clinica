package com.proyecto.appclinica.scheduler;

import com.proyecto.appclinica.service.MedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationCheckScheduler {
    private final MedicationService medicationService;

    /**
     * Tarea programada que se ejecuta cada 5 minutos para verificar medicamentos pendientes
     * y enviar notificaciones si están fuera del rango de tolerancia configurado
     */
    @Scheduled(fixedRate = 180000) // 3 minutos = 180000 ms
    public void checkMedications() {
        log.debug("Iniciando verificación programada de medicamentos pendientes");
        medicationService.checkPendingMedications();
    }
}
