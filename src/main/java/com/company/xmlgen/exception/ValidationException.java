package com.company.xmlgen.exception;

import java.util.List;

/**
 * Thrown when input validation fails. May carry multiple field-level violations.
 */
public class ValidationException extends ApplicationException {

    private final List<FieldViolation> violations;

    public ValidationException(List<FieldViolation> violations) {
        super(CommonErrorCode.VALIDATION_FAILED);
        this.violations = List.copyOf(violations);
    }

    public ValidationException(ErrorCode errorCode, String field, String message) {
        super(errorCode, field, message);
        this.violations = List.of(FieldViolation.of(field, errorCode.code(), message));
    }

    public List<FieldViolation> getViolations() {
        return violations;
    }
}
