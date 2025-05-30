package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.PatientProfileResponse;
import com.proyecto.appclinica.model.dto.auth.*;
import com.proyecto.appclinica.service.impl.AuthServiceImpl;
import com.proyecto.appclinica.service.CodeVerificationService;
import com.proyecto.appclinica.service.impl.LoginServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthServiceImpl authService;
    private final LoginServiceImpl loginService;
    private final CodeVerificationService codeService;

    @GetMapping("/check-user")
    public ResponseEntity<CodeSubmissionResponseDto> checkUserExists(@RequestParam String identifier) {
        return ResponseEntity.ok(authService.checkUserExists(identifier));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<VerifyCodeResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(authService.verifyCode(request.getIdentifier(), request.getCode()));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<CodeSubmissionResponseDto> resendCode(@RequestParam String identifier) {
        return ResponseEntity.ok(authService.resendCode(identifier));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    @GetMapping("/user")
    public ResponseEntity<PatientProfileResponse> getAuthenticatedUser() {
        return ResponseEntity.ok(authService.getAuthenticatedUser());
    }
}
