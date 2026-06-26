package com.company.xmlgen.exception;

/**
 * Thrown when a business rule is violated.
 */
public class BusinessException extends ApplicationException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
