package com.company.xmlgen.xmlgeneration.service;

/**
 * Field-level validation error for runtime template validation.
 */
public record RuntimeValidationError(String fieldName, String code, String message) {
}
