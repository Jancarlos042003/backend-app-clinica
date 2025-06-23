package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.ChatRequest;
import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> chat(ChatRequest chatRequest);
}
