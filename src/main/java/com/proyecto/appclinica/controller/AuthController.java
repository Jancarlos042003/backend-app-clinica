package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.service.AuthService;
import com.proyecto.appclinica.service.CodeVerificationService;
import com.proyecto.appclinica.model.dto.ApiResponse;
import com.proyecto.appclinica.model.dto.LoginRequest;
import com.proyecto.appclinica.model.dto.VerifyCodeRequest;
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
    public ResponseEntity<ApiResponse<Boolean>> checkUserExists(@Valid @RequestBody LoginRequest request) {
        boolean exists = authService.userExists(request.getIdentifier());
        return ResponseEntity.ok(new ApiResponse<>(exists,
                exists ? "Usuario encontrado" : "Usuario no encontrado"));
    }

    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@Valid @RequestBody LoginRequest request) {
        codeService.generateAndSendCode(request.getIdentifier());
        return ResponseEntity.ok(new ApiResponse<>(true, "Código enviado correctamente"));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<String>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        String token = authService.verifyCodeAndGetToken(request.getIdentifier(), request.getCode());
        return ResponseEntity.ok(new ApiResponse<>(token, "Código verificado correctamente"));
    }

}
