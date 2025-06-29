package com.proyecto.appclinica.exception;

/**
 * Excepción que se lanza cuando las contraseñas no coinciden durante el proceso
 * de creación o actualización de credenciales.
 */
public class PasswordMismatchException extends RuntimeException {

    public PasswordMismatchException(String message) {
        super(message);
    }

    public PasswordMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
