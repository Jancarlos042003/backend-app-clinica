package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.ChatRequest;
import com.proyecto.appclinica.model.dto.DocumentUploadResponse;
import com.proyecto.appclinica.service.ChatService;
import com.proyecto.appclinica.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final DocumentIngestionService ingestionService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> sendMessage(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestParam(name = "file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestionService.documentIngestion(file));
    }
}
