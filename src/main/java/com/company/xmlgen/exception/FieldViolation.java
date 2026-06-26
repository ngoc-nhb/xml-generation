package com.company.xmlgen.exception;

/**
 * Domain-neutral validation violation. Carries no presentation-layer types.
 * Translation to {@link com.company.xmlgen.common.api.ApiError} happens at the HTTP boundary.
 */
public record FieldViolation(String field, String code, String message) {

    public static FieldViolation of(String field, String code, String message) {
        return new FieldViolation(field, code, message);
    }

    public static FieldViolation of(String code, String message) {
        return new FieldViolation(null, code, message);
    }
}
