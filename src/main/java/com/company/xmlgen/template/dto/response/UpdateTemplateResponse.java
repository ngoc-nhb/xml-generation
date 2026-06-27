package com.company.xmlgen.template.dto.response;

import com.company.xmlgen.template.entity.TemplateStatus;
import java.time.Instant;

/**
 * Response {@code data} payload for {@code PUT /api/v1/templates/{id}}.
 *
 * @see docs/06-api-design/p3_template-api.md §25
 */
public record UpdateTemplateResponse(
        Long id,
        String code,
        String name,
        String description,
        TemplateStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
