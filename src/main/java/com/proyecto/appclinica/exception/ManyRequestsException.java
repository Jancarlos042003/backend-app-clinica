package com.proyecto.appclinica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
public class ManyRequestsException extends RuntimeException {
    public ManyRequestsException(String message) {
        super(message);
    }
}
