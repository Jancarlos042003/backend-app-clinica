package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.jwt.JwtUtils;
import com.proyecto.appclinica.model.dto.auth.AuthResponseDto;
import com.proyecto.appclinica.model.dto.auth.RefreshTokenRequestDto;
import com.proyecto.appclinica.model.entity.RefreshToken;
import com.proyecto.appclinica.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl {

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthResponseDto refreshToken(RefreshTokenRequestDto requestDTO){
        // Obtenemos el token
        String refreshToken = requestDTO.refreshToken();

        try {
            // Validar el refresh token
            if (!jwtUtils.isTokenValid(refreshToken)) {
                return null;
            }

            // Extraer el username del refresh token
            String username = jwtUtils.getUsernameFromToken(refreshToken);

            if (!isRefreshTokenValid(username, refreshToken)) {
                return null;
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());

            // Generar nuevo access token
            String newAccessToken = jwtUtils.createTokenFromUsernameWithCustomClaims(userDetails.getUsername(), claims);

            // Opcionalmente, generar nuevo refresh token (rotación de tokens)
            String newRefreshToken = jwtUtils.createRefreshToken(userDetails.getUsername());
            updateRefreshToken(userDetails.getUsername(), refreshToken, newRefreshToken);

            return AuthResponseDto.builder()
                    .message("Token refreshed successfully")
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveRefreshToken(String username, String refreshToken){
        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .username(username)
                .expiryDate(calculateExpiryDate())
                .build();

        refreshTokenRepository.save(token);
    }

    private boolean isRefreshTokenValid(String username, String refreshToken){
        // Verificar si existe en la BD y es válido
        RefreshToken token = refreshTokenRepository.findByUsernameAndToken(username, refreshToken).orElse(null);

        return token != null && !token.isExpired();
    }

    private void updateRefreshToken(String username, String oldToken, String newToken){
        refreshTokenRepository.findByUsernameAndToken(username, oldToken)
                .ifPresent(token -> {
                    token.setToken(newToken);
                    token.setExpiryDate(calculateExpiryDate());

                    refreshTokenRepository.save(token);
                });
    }

    private Date calculateExpiryDate(){
        return new Date(System.currentTimeMillis() + refreshTokenExpiration);
    }
}