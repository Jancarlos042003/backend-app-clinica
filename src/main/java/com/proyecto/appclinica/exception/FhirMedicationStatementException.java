package com.proyecto.appclinica.exception;

public class FhirMedicationStatementException extends RuntimeException {

    public FhirMedicationStatementException(String message) {
        super(message);
    }

    public FhirMedicationStatementException(String message, Throwable cause) {
        super(message, cause);
    }
}
