package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ApplicationException;

/**
 * Raised when value resolution cannot be executed.
 */
public class ValueResolutionException extends ApplicationException {

    private final ValueResolutionErrorCode resolutionErrorCode;

    public ValueResolutionException(ValueResolutionErrorCode errorCode, String message) {
        super(errorCode, message);
        this.resolutionErrorCode = errorCode;
    }

    public ValueResolutionErrorCode getResolutionErrorCode() {
        return resolutionErrorCode;
    }
}
