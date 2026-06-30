package com.company.xmlgen.xmlgeneration.dto;

import java.util.List;

/**
 * Application response for XML export.
 *
 * <p>Application boundary only. Does not expose Runtime Engine internal models.
 */
public record ExportResponse(boolean successful, String xml, List<ExportValidationError> validationErrors) {

    public ExportResponse {
        validationErrors = validationErrors == null ? List.of() : List.copyOf(validationErrors);
    }

    public static ExportResponse success(String xml) {
        return new ExportResponse(true, xml, List.of());
    }

    public static ExportResponse validationFailed(List<ExportValidationError> validationErrors) {
        return new ExportResponse(false, null, validationErrors);
    }
}
