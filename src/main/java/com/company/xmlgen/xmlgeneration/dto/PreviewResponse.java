package com.company.xmlgen.xmlgeneration.dto;

import java.util.List;

/**
 * Application response for XML preview.
 *
 * <p>Application boundary only. Does not expose Runtime Engine internal models.
 */
public record PreviewResponse(
        boolean successful,
        String xml,
        List<PreviewValidationError> validationErrors,
        List<PreviewExecutionNodeResponse> executionTree) {

    public PreviewResponse {
        validationErrors = validationErrors == null ? List.of() : List.copyOf(validationErrors);
        executionTree = executionTree == null ? List.of() : List.copyOf(executionTree);
    }

    public static PreviewResponse success(String xml, List<PreviewExecutionNodeResponse> executionTree) {
        return new PreviewResponse(true, xml, List.of(), executionTree);
    }

    public static PreviewResponse validationFailed(List<PreviewValidationError> validationErrors) {
        return new PreviewResponse(false, null, validationErrors, List.of());
    }
}
