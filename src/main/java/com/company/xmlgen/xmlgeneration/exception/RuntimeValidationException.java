package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ApplicationException;

/**
 * Raised when runtime validation cannot be executed.
 */
public class RuntimeValidationException extends ApplicationException {

    private final RuntimeValidationErrorCode validationErrorCode;

    public RuntimeValidationException(RuntimeValidationErrorCode errorCode, String message) {
        super(errorCode, message);
        this.validationErrorCode = errorCode;
    }

    public RuntimeValidationErrorCode getValidationErrorCode() {
        return validationErrorCode;
    }
}
