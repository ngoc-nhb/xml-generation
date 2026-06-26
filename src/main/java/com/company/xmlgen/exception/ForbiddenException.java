package com.company.xmlgen.exception;

/**
 * Thrown when the authenticated caller lacks permission for the requested operation.
 */
public class ForbiddenException extends ApplicationException {

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
