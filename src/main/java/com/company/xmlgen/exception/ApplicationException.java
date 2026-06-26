package com.company.xmlgen.exception;

/**
 * Base type for application exceptions carrying a stable error code.
 * Translated to HTTP responses by {@link GlobalExceptionHandler}.
 */
public abstract class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String field;

    protected ApplicationException(ErrorCode errorCode) {
        this(errorCode, null, null, null);
    }

    protected ApplicationException(ErrorCode errorCode, String message) {
        this(errorCode, null, message, null);
    }

    protected ApplicationException(ErrorCode errorCode, String field, String message) {
        this(errorCode, field, message, null);
    }

    protected ApplicationException(ErrorCode errorCode, String field, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.field = field;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getField() {
        return field;
    }
}
