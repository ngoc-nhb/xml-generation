package com.company.xmlgen.exception;

/**
 * Thrown when a resource conflict (e.g. optimistic-locking failure, duplicate code) is detected.
 */
public class ConflictException extends ApplicationException {

    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
