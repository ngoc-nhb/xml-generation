package com.company.xmlgen.xmlgeneration.controller;

import com.company.xmlgen.common.api.ApiError;
import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.xmlgeneration.dto.PreviewValidationError;
import com.company.xmlgen.xmlgeneration.dto.request.PreviewRequest;
import com.company.xmlgen.xmlgeneration.service.PreviewService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for XML preview.
 *
 * <p>Transport layer only. Delegates orchestration to {@link PreviewService}.
 *
 * @see docs/06-api-design/p3_template-api.md §26A
 */
@RestController
@RequestMapping("/api/v1/templates")
public class PreviewController {

    private final PreviewService previewService;

    public PreviewController(PreviewService previewService) {
        this.previewService = previewService;
    }

    @PostMapping("/{templateId}/preview")
    public ResponseEntity<ApiResponse<?>> preview(
            @PathVariable Long templateId, @RequestBody(required = false) PreviewRequest request) {
        PreviewRequest body = request == null ? new PreviewRequest(null, null) : request;

        com.company.xmlgen.xmlgeneration.dto.PreviewResponse serviceResponse = previewService.preview(
                new com.company.xmlgen.xmlgeneration.dto.PreviewRequest(
                        templateId, body.inputData(), body.selectedMasterData()));

        if (!serviceResponse.successful()) {
            return ResponseEntity.ok(ApiResponse.failure(toApiErrors(serviceResponse.validationErrors())));
        }

        return ResponseEntity.ok(ApiResponse.ok(new com.company.xmlgen.xmlgeneration.dto.response.PreviewResponse(
                serviceResponse.xml())));
    }

    private static List<ApiError> toApiErrors(List<PreviewValidationError> validationErrors) {
        return validationErrors.stream()
                .map(error -> ApiError.of(error.fieldName(), error.code()))
                .toList();
    }
}
