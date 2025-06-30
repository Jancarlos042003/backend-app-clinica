package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.model.dto.ChatRequest;
import com.proyecto.appclinica.model.entity.MessageEntity;
import com.proyecto.appclinica.repository.MessageRepository;
import com.proyecto.appclinica.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatClient chatClient;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public Flux<String> chat(ChatRequest chatRequest) {
        String sessionId = chatRequest.getSessionId();
        String userMessage = chatRequest.getMessage();

        // Validar la petición
        if (!isValidRequest(userMessage)) {
            return Flux.error(new IllegalArgumentException("El mensaje no puede estar vacío."));
        }

        try {
            // Construir la lista de mensajes del historial
            List<Message> messages = buildHistory(sessionId);

            // Guardar el mensaje del usuario en la base de datos
            saveUserInput(sessionId, userMessage);

            // Procesar la solicitud de texto
            return processRequest(sessionId, userMessage, messages);
        } catch (Exception e) {
            log.error("Error inesperado en el procesamiento del mensaje: {}", e.getMessage());
            return Flux.error(new RuntimeException("Error en el procesamiento: " + e.getMessage()));
        }
    }

    /**
     * Valida que la solicitud tenga al menos un texto.
     */
    private boolean isValidRequest(String userMessage) {
        boolean hasText = userMessage != null && !userMessage.trim().isEmpty();
        return hasText;
    }

    /**
     * Guarda la entrada del usuario en la base de datos.
     */
    private void saveUserInput(String sessionId, String userMessage) {
        boolean hasText = userMessage != null && !userMessage.trim().isEmpty();

        if (hasText) {
            saveMessage("usuario", userMessage, sessionId);
        }
    }

    /**
     * Procesa la solicitud de texto.
     */
    private Flux<String> processRequest(String sessionId, String userMessage, List<Message> messages) {
        StringBuilder responseBuilder = new StringBuilder();

        return chatClient.prompt()
                .messages(messages)
                .user(userMessage)
                .stream()
                .content()
                .doOnNext(responseBuilder::append) // Acumula la respuesta en el StringBuilder
                .doOnComplete(() -> {
                    String response = responseBuilder.toString();

                    // Guardar la respuesta del asistente en la base de datos
                    saveMessage("asistente", response, sessionId);
                })
                .doOnError(error -> log.error("Error al procesar el mensaje: {}", error.getMessage()));
    }

    /**
     * Guarda un mensaje en la base de datos.
     */
    private void saveMessage(String rol, String message, String sessionId) {
        messageRepository.save(MessageEntity.builder()
                .rol(rol)
                .content(message)
                .createdAt(LocalDateTime.now())
                .sessionId(sessionId)
                .build());
    }

    /**
     * Construye el historial de mensajes para una sesión dada.
     */
    private List<Message> buildHistory(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::convertAMessage)
                .toList();
    }

    /**
     * Convierte una entidad de mensaje a un objeto Message para el modelo AI.
     */
    private Message convertAMessage(MessageEntity messageEntity) {
        return switch (messageEntity.getRol()) {
            case "usuario" -> new UserMessage(messageEntity.getContent());
            case "asistente" -> new AssistantMessage(messageEntity.getContent());
            default -> {
                log.warn("Rol desconocido: {}", messageEntity.getRol());
                yield new UserMessage(messageEntity.getContent()); // Por defecto, tratamos como mensaje de usuario
            }
        };
    }
}