package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.auth.CredentialsRequestDto;
import com.proyecto.appclinica.service.impl.CredentialsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/credentials")
public class CredentialsController {
    private final CredentialsServiceImpl credentialsService;

    @PostMapping
    public ResponseEntity<String> createCredentials(@Valid @RequestBody CredentialsRequestDto requestDto){
        credentialsService.createCredentials(requestDto);
        return ResponseEntity.ok("Credenciales creadas correctamente");
    }
}
