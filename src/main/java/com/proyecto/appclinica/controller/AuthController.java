package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.LoginRequest;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeRequest;
import com.proyecto.appclinica.model.dto.auth.AuthResponseDto;
import com.proyecto.appclinica.model.dto.auth.CodeSubmissionResponseDto;
import com.proyecto.appclinica.model.dto.auth.ResponseUserExistsDto;
import com.proyecto.appclinica.service.AuthService;
import com.proyecto.appclinica.service.CodeVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CodeVerificationService codeService;

    @PostMapping("/check-user")
    public ResponseEntity<ResponseUserExistsDto> checkUserExists(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.checkUserExists(request.getIdentifier()));
    }

    @PostMapping("/send-code")
    public ResponseEntity<CodeSubmissionResponseDto> sendVerificationCode(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(codeService.generateAndSendCode(request.getIdentifier()));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<AuthResponseDto> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(authService.verifyCodeAndGetToken(request.getIdentifier(), request.getCode()));
    }
}
