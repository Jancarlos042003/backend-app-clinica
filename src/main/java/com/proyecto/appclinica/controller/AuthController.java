package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.IdentifierRequest;
import com.proyecto.appclinica.model.dto.auth.CodeSubmissionResponseDto;
import com.proyecto.appclinica.model.dto.auth.ResponseUserExistsDto;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeRequest;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeResponse;
import com.proyecto.appclinica.service.impl.AuthServiceImpl;
import com.proyecto.appclinica.service.CodeVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthServiceImpl authService;
    private final CodeVerificationService codeService;

    @GetMapping("/check-user")
    public ResponseEntity<ResponseUserExistsDto> checkUserExists(@Valid @RequestBody IdentifierRequest request) {
        return ResponseEntity.ok(authService.checkUserExists(request.getIdentifier()));
    }

    @PostMapping("/send-code")
    public ResponseEntity<CodeSubmissionResponseDto> sendVerificationCode(@Valid @RequestBody IdentifierRequest request) {
        return ResponseEntity.ok(codeService.generateAndSendCode(request.getIdentifier()));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<VerifyCodeResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(authService.verifyCode(request.getIdentifier(), request.getCode()));
    }
}
