package com.company.xmlgen.template.dto.response;

import com.company.xmlgen.template.entity.TemplateStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

/**
 * Response {@code data} payload for {@code GET /api/v1/templates/{id}}.
 *
 * @see docs/06-api-design/p3_template-api.md §23
 */
public record TemplateResponse(
        Long id,
        String code,
        String name,
        String description,
        TemplateStatus status,
        Instant createdAt,
        Instant updatedAt,
        TemplateSchemaResponse schema,
        JsonNode sampleInputJson) {
}
