package com.company.xmlgen.template.dto.response;

import com.company.xmlgen.template.entity.TemplateStatus;

/**
 * Response {@code data} payload for {@code GET /api/v1/templates/{id}} (metadata only).
 *
 * @see docs/06-api-design/p3_template-api.md §23
 */
public record TemplateResponse(
        Long id,
        String templateCode,
        String templateName,
        String description,
        TemplateStatus status) {
}
