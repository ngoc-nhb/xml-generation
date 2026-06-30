package com.company.xmlgen.xmlgeneration.dto;

/**
 * Validation error exposed by the Preview application boundary.
 */
public record PreviewValidationError(String fieldName, String code, String message) {
}
