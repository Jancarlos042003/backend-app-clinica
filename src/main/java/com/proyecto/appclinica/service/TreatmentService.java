package com.proyecto.appclinica.service;

import ca.uhn.fhir.context.FhirContext;
import com.proyecto.appclinica.model.dto.treatment.CreateTreatmentDto;
import com.proyecto.appclinica.model.dto.treatment.UpdateTreatmentDto;
import com.proyecto.appclinica.repository.FhirMedicationRequestRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TreatmentService {
    private final FhirMedicationRequestRepository fhirMedicationRequestRepository;

    @Getter
    private final FhirContext fhirContext;

    public MedicationRequest createTreatment(CreateTreatmentDto treatmentDTO){
        MedicationRequest request = new MedicationRequest();

        // Referencia al paciente
        request.setSubject(new Reference("Patient/" + treatmentDTO.getPatientId())); // Formato: "Patient/[ID]"

        // Medicamento
        request.setMedication(
                new CodeableConcept().setText(treatmentDTO.getNameMedicine())
        );

        // Dosis estructurada
        Dosage dosage = new Dosage();
        dosage.addDoseAndRate()
                .setDose(new Quantity()
                        .setValue(treatmentDTO.getDoseValue())
                        .setUnit(treatmentDTO.getDoseUnit())
                        .setSystem("http://unitsofmeasure.org") // UCUM
                );

        // Timing
        Timing timing = new Timing();
        timing.setRepeat(new Timing.TimingRepeatComponent()
                .setFrequency(treatmentDTO.getFrequencyHours())
                .setPeriodUnit(Timing.UnitsOfTime.H)
        );
        dosage.setTiming(timing);

        // Período de validez (fecha inicio + duración)
        Date startDate = Date.from(treatmentDTO.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(treatmentDTO.getStartDate().plusDays(treatmentDTO.getDuration())
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest =
                new MedicationRequest.MedicationRequestDispenseRequestComponent();

        dispenseRequest.setValidityPeriod(new Period()
                .setStart(startDate)
                .setEnd(endDate)
        );

        request.setDispenseRequest(dispenseRequest);

        request.addDosageInstruction(dosage);
        request.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        request.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

        return fhirMedicationRequestRepository.saveMedicationRequest(request);
    }

    public MedicationRequest updateTreatment(String medicationRequestId, UpdateTreatmentDto treatmentDTO) {
        MedicationRequest existingRequest = fhirMedicationRequestRepository.getMedicationRequestById(medicationRequestId);

        // Actualizar los campos necesarios
        existingRequest.setMedication(
                new CodeableConcept().setText(treatmentDTO.getNameMedicine())
        );

        Dosage dosage = new Dosage();
        dosage.addDoseAndRate()
                .setDose(new Quantity()
                        .setValue(treatmentDTO.getDoseValue())
                        .setUnit(treatmentDTO.getDoseUnit())
                        .setSystem("http://unitsofmeasure.org")
                );

        Timing timing = new Timing();
        timing.setRepeat(new Timing.TimingRepeatComponent()
                .setFrequency(treatmentDTO.getFrequencyHours())
                .setPeriodUnit(Timing.UnitsOfTime.H)
        );
        dosage.setTiming(timing);

        Date startDate = Date.from(treatmentDTO.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(treatmentDTO.getStartDate().plusDays(treatmentDTO.getDuration())
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest =
                new MedicationRequest.MedicationRequestDispenseRequestComponent();

        dispenseRequest.setValidityPeriod(new Period()
                .setStart(startDate)
                .setEnd(endDate)
        );

        existingRequest.setDispenseRequest(dispenseRequest);
        existingRequest.addDosageInstruction(dosage);

        return fhirMedicationRequestRepository.updateMedicationRequest(existingRequest);
    }

    public MedicationRequest cancelTreatment(String medicationRequestId){
        MedicationRequest medicationRequest = fhirMedicationRequestRepository.getMedicationRequestById(medicationRequestId);

        // Cambiar el estado a CANCELLED
        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.CANCELLED);

        // Establecer la razón de cancelación (Opcional)
        medicationRequest.setStatusReason(new CodeableConcept().setText("Tratamiento cancelado por solicitud"));

        return fhirMedicationRequestRepository.updateMedicationRequest(medicationRequest);
    }

    public MedicationRequest completeTreatment(String medicationRequestId) {
        MedicationRequest medicationRequest = fhirMedicationRequestRepository.getMedicationRequestById(medicationRequestId);

        // Cambiar el estado a COMPLETED
        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);

        medicationRequest.setStatusReason(new CodeableConcept().setText("Tratamiento completado según lo prescrito"));

        return fhirMedicationRequestRepository.updateMedicationRequest(medicationRequest);
    }

    public List<MedicationRequest> getAllMedicationRequestsByPatientId(String patientId) {
        return fhirMedicationRequestRepository.findByPatientId(patientId);
    }

    public List<MedicationRequest> getAllMedicationRequestsByPatientIdAndStatus(String patientId, String status) {
        return fhirMedicationRequestRepository.findByPatientIdAndStatus(patientId, status);
    }
}
