package com.company.xmlgen.exception;

/**
 * Thrown when the caller is not authenticated.
 */
public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
