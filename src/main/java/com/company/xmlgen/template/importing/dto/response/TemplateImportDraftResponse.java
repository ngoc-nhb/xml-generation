package com.company.xmlgen.template.importing.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Non-persisted template draft returned by {@code POST /api/v1/templates/import}.
 */
public record TemplateImportDraftResponse(
        String suggestedCode,
        String suggestedName,
        String sourceFileName,
        List<TemplateImportDraftFieldResponse> fields,
        JsonNode sampleInputJson) {}
