package com.proyecto.appclinica.event.patient;

import com.proyecto.appclinica.model.dto.PatientHistoryResponse;
import com.proyecto.appclinica.service.PatientHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatientHistoryEventListener {
    private final PatientHistoryService patientHistoryService;

    @EventListener
    @Async
    public void createPatientHistory(PatientHistoryEvent event) {
        PatientHistoryResponse historyResponse = patientHistoryService.createPatientHistory(event.getPatientId());
        log.info("{} - {}", historyResponse.message(), historyResponse.patientId());
    }
}
