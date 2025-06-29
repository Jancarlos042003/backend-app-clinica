package com.proyecto.appclinica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StatusException extends RuntimeException {
    public StatusException(String message) {
        super(message);
    }
}
