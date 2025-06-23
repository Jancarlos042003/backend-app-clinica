package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.ChatRequest;
import com.proyecto.appclinica.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(@RequestBody ChatRequest request){
        return chatService.chat(request);
    }
}
