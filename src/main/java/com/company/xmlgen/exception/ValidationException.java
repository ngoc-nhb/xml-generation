package com.company.xmlgen.exception;

import com.company.xmlgen.common.api.ApiError;

import java.util.List;

/**
 * Thrown when input validation fails. May carry multiple field-level errors.
 */
public class ValidationException extends ApplicationException {

    private final List<ApiError> errors;

    public ValidationException(List<ApiError> errors) {
        super(CommonErrorCode.VALIDATION_FAILED);
        this.errors = List.copyOf(errors);
    }

    public ValidationException(ErrorCode errorCode, String field, String message) {
        super(errorCode, field, message);
        this.errors = List.of(ApiError.of(field, errorCode.code()));
    }

    public List<ApiError> getErrors() {
        return errors;
    }
}
