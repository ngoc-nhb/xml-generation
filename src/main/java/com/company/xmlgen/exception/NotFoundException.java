package com.company.xmlgen.exception;

/**
 * Thrown when a requested resource does not exist.
 */
public class NotFoundException extends ApplicationException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
