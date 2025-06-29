package com.proyecto.appclinica.event.user;

import com.proyecto.appclinica.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSettingsCreationEventListener {

    private final UserSettingsService userSettingsService;

    @Async
    @EventListener
    public void handleUserSettingsCreation(UserSettingsCreationEvent event) {
        log.info("Creando configuraciones de usuario para el usuario con ID: {}", event.getUserId());
        try {
            userSettingsService.createUserSettings(event.getUserId());
            log.info("Configuraciones de usuario creadas exitosamente para el usuario con ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error al crear configuraciones para el usuario con ID: {}", event.getUserId(), e);
            // Implementar reintentos o notificaciones según sea necesario
        }
    }
}
