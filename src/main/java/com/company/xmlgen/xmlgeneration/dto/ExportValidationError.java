package com.company.xmlgen.xmlgeneration.dto;

/**
 * Validation error exposed by the Export application boundary.
 */
public record ExportValidationError(String fieldName, String code, String message) {
}
