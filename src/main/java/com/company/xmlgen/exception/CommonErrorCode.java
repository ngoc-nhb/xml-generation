package com.company.xmlgen.exception;

/**
 * Cross-cutting error codes used by the GlobalExceptionHandler.
 * Module-specific codes live in their respective modules.
 */
public enum CommonErrorCode implements ErrorCode {

    VALIDATION_FAILED,
    FORBIDDEN,
    UNAUTHORIZED,
    NOT_FOUND,
    CONFLICT,
    PAYLOAD_TOO_LARGE,
    STORAGE_ERROR,
    INTERNAL_SERVER_ERROR;

    @Override
    public String code() {
        return name();
    }
}
