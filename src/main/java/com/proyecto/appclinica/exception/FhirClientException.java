package com.proyecto.appclinica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FhirClientException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FhirClientException(String message) {
        super(message);
    }

    public FhirClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
