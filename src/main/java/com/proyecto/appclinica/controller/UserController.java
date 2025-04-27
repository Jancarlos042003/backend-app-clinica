package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.ApiResponse;
import com.proyecto.appclinica.model.dto.UserProfileResponse;
import com.proyecto.appclinica.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(Authentication authentication) {
        String userId = authentication.getName();
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(new ApiResponse<>(profile, "Perfil de usuario obtenido"));
    }
}
