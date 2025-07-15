package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.InvalidRequestException;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.EmergencyContactCreateDTO;
import com.proyecto.appclinica.model.dto.EmergencyContactResponseDTO;
import com.proyecto.appclinica.model.dto.MedicationSettingsDTO;
import com.proyecto.appclinica.model.dto.UserSettingsResponseDTO;
import com.proyecto.appclinica.model.entity.EmergencyContact;
import com.proyecto.appclinica.model.entity.MedicationSettings;
import com.proyecto.appclinica.model.entity.UserSettings;
import com.proyecto.appclinica.repository.UserSettingsRepository;
import com.proyecto.appclinica.service.UserSettingsService;
import com.proyecto.appclinica.util.UserSettingsMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    @Override
    public UserSettings getUserSettings(String patientId) {
        return userSettingsRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSettings", "paciente con ID", patientId));
    }

    @Override
    public UserSettingsResponseDTO getUserSettingsResponse(String patientId) {
        UserSettings settings = getUserSettings(patientId);
        return UserSettingsMapper.toResponseDto(settings);
    }

    @Override
    @Transactional
    public UserSettings createUserSettings(String patientId) {
        if (patientId == null) {
            throw new InvalidRequestException("El ID de usuario no puede ser nulo");
        }

        // Verificar si ya existe
        if (userSettingsRepository.findByPatientId(patientId).isPresent()) {
            return getUserSettings(patientId);
        }

        // Crear configuraciones con valores por defecto
        MedicationSettings medicationSettings = MedicationSettings.builder().build();
        UserSettings userSettings = UserSettings.builder()
                .patientId(patientId)
                .medicationSettings(medicationSettings)
                .build();

        return userSettingsRepository.save(userSettings);
    }

    @Override
    public MedicationSettingsDTO updateMedicationSettings(String patientId, @Valid MedicationSettingsDTO medicationSettingsDTO) {
        UserSettings userSettings = getUserSettings(patientId);

        // Validar que reminderFrequencyMinutes no sea mayor que toleranceWindowMinutes
        if (medicationSettingsDTO.getReminderFrequencyMinutes() > medicationSettingsDTO.getToleranceWindowMinutes()) {
            medicationSettingsDTO.setReminderFrequencyMinutes(medicationSettingsDTO.getToleranceWindowMinutes());
        }

        // Convertir DTO a entidad
        MedicationSettings medicationSettings = UserSettingsMapper.toEntity(medicationSettingsDTO);

        // Actualizar configuraciones de medicación
        MedicationSettings existingSettings = userSettings.getMedicationSettings();
        if (existingSettings == null) {
            userSettings.setMedicationSettings(medicationSettings);
        } else {
            existingSettings.setToleranceWindowMinutes(medicationSettings.getToleranceWindowMinutes());
            existingSettings.setReminderFrequencyMinutes(medicationSettings.getReminderFrequencyMinutes());
            existingSettings.setMaxReminderAttempts(medicationSettings.getMaxReminderAttempts());
        }

        userSettingsRepository.save(userSettings);
        return UserSettingsMapper.toDto(userSettings.getMedicationSettings());
    }

    @Override
    public MedicationSettingsDTO getMedicationSettings(String patientId) {
        UserSettings userSettings = getUserSettings(patientId);
        return UserSettingsMapper.toDto(userSettings.getMedicationSettings());
    }

    @Override
    public List<EmergencyContactResponseDTO> getEmergencyContacts(String patientId) {
        UserSettings userSettings = getUserSettings(patientId);
        return userSettings.getEmergencyContacts().stream()
                .map(UserSettingsMapper::toResponseDto)
                .toList();
    }

    @Override
    public EmergencyContactResponseDTO addEmergencyContact(String patientId, EmergencyContactCreateDTO contactDTO) {
        UserSettings userSettings = getUserSettings(patientId);

        // Validar el número de teléfono
        if (!isValidPhoneNumber(contactDTO.getPhoneNumber())) {
            throw new InvalidRequestException("El número de teléfono debe comenzar con 9 y tener 9 dígitos");
        }

        // Validar que el usuario no tenga ya este número
        boolean phoneExists = userSettings.getEmergencyContacts().stream()
                .anyMatch(c -> c.getPhoneNumber().equals(contactDTO.getPhoneNumber()));

        if (phoneExists) {
            throw new InvalidRequestException("Ya existe un contacto de emergencia con este número de teléfono");
        }

        // Convertir DTO a entidad
        EmergencyContact contact = UserSettingsMapper.toEntity(contactDTO);

        userSettings.addEmergencyContact(contact);
        UserSettings savedSettings = userSettingsRepository.save(userSettings);

        // Obtener el contacto recién guardado con el ID asignado
        EmergencyContact savedContact = savedSettings.getEmergencyContacts().stream()
                .filter(c -> c.getPhoneNumber().equals(contactDTO.getPhoneNumber())
                        && c.getName().equals(contactDTO.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error al recuperar el contacto guardado"));

        // Convertir la entidad guardada de vuelta a DTO para la respuesta
        return UserSettingsMapper.toResponseDto(savedContact);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("9\\d{8}");
    }

    @Override
    @Transactional
    public EmergencyContactResponseDTO updateEmergencyContact(String patientId, Long contactId, EmergencyContactCreateDTO updatedContactDTO) {
        UserSettings userSettings = getUserSettings(patientId);

        EmergencyContact contact = userSettings.getEmergencyContacts().stream()
                .filter(c -> c.getId().equals(contactId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "ID", contactId));

        contact.setName(updatedContactDTO.getName());
        contact.setPhoneNumber(updatedContactDTO.getPhoneNumber());
        contact.setRelationship(updatedContactDTO.getRelationship());

        userSettingsRepository.save(userSettings);
        return UserSettingsMapper.toResponseDto(contact);
    }

    @Override
    @Transactional
    public void deleteEmergencyContact(String patientId, Long contactId) {
        UserSettings settings = getUserSettings(patientId);

        EmergencyContact contact = settings.getEmergencyContacts().stream()
                .filter(c -> c.getId().equals(contactId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "ID", contactId));

        settings.removeEmergencyContact(contact);
        userSettingsRepository.save(settings);
    }
}
