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

import java.time.Duration;
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
            log.warn("Mensaje inválido recibido: {}", userMessage);
            return Flux.just("data: Error: El mensaje no puede estar vacío\n\n");
        }

        try {
            // Construir la lista de mensajes del historial
            List<Message> messages = buildHistory(sessionId);

            // Guardar el mensaje del usuario en la base de datos
            saveUserInput(sessionId, userMessage);

            // Procesar la solicitud de texto
            return processRequest(sessionId, userMessage, messages);
        } catch (Exception e) {
            log.error("Error inesperado en el procesamiento del mensaje: {}", e.getMessage(), e);
            return Flux.just("data: Error en el procesamiento: " + e.getMessage() + "\n\n");
        }
    }

    /**
     * Valida que la solicitud tenga al menos un texto.
     */
    private boolean isValidRequest(String userMessage) {
        return userMessage != null && !userMessage.trim().isEmpty();
    }

    /**
     * Guarda la entrada del usuario en la base de datos.
     */
    private void saveUserInput(String sessionId, String userMessage) {
        if (userMessage != null && !userMessage.trim().isEmpty()) {
            saveMessage("usuario", userMessage, sessionId);
        }
    }

    /**
     * Procesa la solicitud de texto y formatea para SSE.
     */
    private Flux<String> processRequest(String sessionId, String userMessage, List<Message> messages) {
        StringBuilder responseBuilder = new StringBuilder();

        return chatClient.prompt()
                .messages(messages)
                .user(userMessage)
                .stream()
                .content()
                .filter(content -> content != null && !content.isEmpty()) // Filtrar contenido vacío
                .buffer(Duration.ofMillis(100)) // Agrupa fragmentos por 100ms
                .map(contentList -> {
                    String content = String.join("", contentList);
                    // Acumular contenido para guardarlo en BD
                    responseBuilder.append(content);
                    // Formatear para Server-Sent Events
                    return content;
                })
                .doOnComplete(() -> {
                    String fullResponse = responseBuilder.toString();

                    // Guardar la respuesta completa del asistente en la base de datos
                    if (!fullResponse.isEmpty()) {
                        saveMessage("asistente", fullResponse, sessionId);
                    }
                })
                .doOnError(error -> {
                    log.error("Error al procesar el mensaje: {}", error.getMessage(), error);
                })
                .concatWith(Flux.just("[DONE]\n\n")) // Señal de finalización
                .onErrorResume(error -> {
                    log.error("Error en el stream: {}", error.getMessage(), error);
                    return Flux.just(
                            "data: Error al procesar la solicitud: " + error.getMessage() + "\n\n",
                            "data: [DONE]\n\n"
                    );
                });
    }

    /**
     * Guarda un mensaje en la base de datos.
     */
    private void saveMessage(String rol, String message, String sessionId) {
        try {
            messageRepository.save(MessageEntity.builder()
                    .rol(rol)
                    .content(message)
                    .createdAt(LocalDateTime.now())
                    .sessionId(sessionId)
                    .build());
        } catch (Exception e) {
            log.error("Error al guardar mensaje en BD: {}", e.getMessage(), e);
        }
    }

    /**
     * Construye el historial de mensajes para una sesión dada.
     */
    private List<Message> buildHistory(String sessionId) {
        try {
            List<MessageEntity> messageEntities = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

            return messageEntities.stream()
                    .map(this::convertAMessage)
                    .toList();
        } catch (Exception e) {
            log.error("Error al construir historial: {}", e.getMessage(), e);
            return List.of(); // Retorna lista vacía en caso de error
        }
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
