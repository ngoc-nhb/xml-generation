package com.company.xmlgen.xmlgeneration.controller;

import com.company.xmlgen.common.api.ApiError;
import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.xmlgeneration.dto.ExportValidationError;
import com.company.xmlgen.xmlgeneration.dto.request.ExportRequest;
import com.company.xmlgen.xmlgeneration.service.ExportService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for XML export.
 *
 * <p>Transport layer only. Delegates orchestration to {@link ExportService}.
 *
 * @see docs/06-api-design/p3_template-api.md §26B
 */
@RestController
@RequestMapping("/api/v1/templates")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/{templateId}/export")
    public ResponseEntity<ApiResponse<?>> export(
            @PathVariable Long templateId, @RequestBody(required = false) ExportRequest request) {
        ExportRequest body = request == null ? new ExportRequest(null, null) : request;

        com.company.xmlgen.xmlgeneration.dto.ExportResponse serviceResponse = exportService.export(
                new com.company.xmlgen.xmlgeneration.dto.ExportRequest(
                        templateId, body.inputData(), body.selectedMasterData()));

        if (!serviceResponse.successful()) {
            return ResponseEntity.ok(ApiResponse.failure(toApiErrors(serviceResponse.validationErrors())));
        }

        return ResponseEntity.ok(ApiResponse.ok(new com.company.xmlgen.xmlgeneration.dto.response.ExportResponse(
                serviceResponse.xml())));
    }

    private static List<ApiError> toApiErrors(List<ExportValidationError> validationErrors) {
        return validationErrors.stream()
                .map(error -> ApiError.of(error.fieldName(), error.code()))
                .toList();
    }
}
