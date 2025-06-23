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

        if (chatRequest.getMessage().trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("El mensaje no pueden estar vacíos."));
        }

        // Guardar el mensaje del usuario en la base de datos
        saveMessage("usuario", chatRequest.getMessage(), sessionId);

        List<Message> messages = buildHistory(sessionId);

        StringBuilder messageCompletion = new StringBuilder();

        // Acumular la respuesta del asistente
        return chatClient.prompt()
                .messages(messages)
                .system("Eres un asistente médico virtual. Responde a las preguntas de los pacientes de manera profesional y empática.")
                .user(chatRequest.getMessage())
                .stream()
                .content()
                .doOnNext(messageCompletion::append) // Acumular el contenido de la respuesta
                .doOnComplete(() -> saveMessage("asistente", messageCompletion.toString(), sessionId)) // Guardar el mensaje del asistente en la base de datos
                .doOnError(error -> log.error("Error al procesar el mensaje: {}", error.getMessage()));
    }

    private void saveMessage(String rol, String message, String sessionId) {
        messageRepository.save(MessageEntity.builder()
                .rol(rol)
                .content(message)
                .createdAt(LocalDateTime.now())
                .sessionId(sessionId)
                .build());
    }

    private List<Message> buildHistory(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::convertAMessage)
                .toList();
    }

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
