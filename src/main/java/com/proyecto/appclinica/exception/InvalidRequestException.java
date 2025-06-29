package com.proyecto.appclinica.exception;

/**
 * Excepción que se lanza cuando los datos de una solicitud son inválidos.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
