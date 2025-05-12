package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.jwt.JwtUtils;
import com.proyecto.appclinica.model.dto.auth.AuthResponseDto;
import com.proyecto.appclinica.model.dto.auth.LoginRequestDto;
import com.proyecto.appclinica.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponseDto login(LoginRequestDto requestDto) {
        try {
            String username = requestDto.getIdentifier();
            String password = requestDto.getPassword();

            // Usuario envía credenciales
            Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);

            // Se pasa al AuthenticationManager para la autenticación
            Authentication authentication = authenticationManager.authenticate(authRequest);

            // Se almacena en el SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Claims personalizados
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());

            // Crear los tokens
            String accessToken = jwtUtils.createTokenFromUsernameWithCustomClaims(userDetails.getUsername(), claims);
            String refreshToken = jwtUtils.createRefreshToken(userDetails.getUsername());

            refreshTokenService.saveRefreshToken(userDetails.getUsername(), refreshToken);

            return AuthResponseDto.builder()
                    .message("Autenticación exitosa")
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Credenciales inválidas para el usuario: {}", requestDto.getIdentifier());
            throw new BadCredentialsException("Credenciales inválidas");
        } catch (UsernameNotFoundException e) {
            log.error("Usuario no encontrado: {}", requestDto.getIdentifier());
            throw new UsernameNotFoundException("Usuario no encontrado");
        } catch (AuthenticationException e) {
            log.error("Error al autenticar el usuario: {}", e.getMessage());
            throw new AuthenticationException("Error al autenticar el usuario") {};
        }
    }
}
