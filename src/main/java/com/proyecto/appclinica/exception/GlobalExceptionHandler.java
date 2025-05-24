package com.proyecto.appclinica.exception;

import com.proyecto.appclinica.model.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCodeException(InvalidCodeException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(ManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleManyRequestsException(ManyRequestsException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(FhirClientException.class)
    public ResponseEntity<ErrorResponse> handleFhirClientException(FhirClientException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    // Todas indican que la AUTENTICACIÓN ha fallado
    @ExceptionHandler({
            BadCredentialsException.class, // Cuando usuario o contraseña son incorrectos
            UsernameNotFoundException.class, // Solo cuando no se encuentra el usuario
            LockedException.class,
            DisabledException.class,
            AccountExpiredException.class,
            CredentialsExpiredException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationExceptions(AuthenticationException ex, WebRequest request) {
        String message = switch (ex) {
            case BadCredentialsException ignored -> "Usuario o contraseña incorrectos";
            case UsernameNotFoundException ignored -> "El usuario no existe";
            case LockedException ignored -> "La cuenta está bloqueada";
            case DisabledException ignored -> "La cuenta está deshabilitada";
            case AccountExpiredException ignored -> "La cuenta ha expirado";
            case CredentialsExpiredException ignored -> "La contraseña ha expirado";
            default -> "Error de autenticación. Intenta más tarde.";
        };

        return createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                message,
                request.getDescription(false)
        );
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message, String path) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .error(status.getReasonPhrase())
                .path(path.replace("uri=", ""))
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
