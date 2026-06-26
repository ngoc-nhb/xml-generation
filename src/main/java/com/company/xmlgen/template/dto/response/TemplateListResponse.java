package com.company.xmlgen.template.dto.response;

import com.company.xmlgen.template.entity.TemplateStatus;

/**
 * List item payload for {@code GET /api/v1/templates}.
 *
 * @see docs/06-api-design/p3_template-api.md §22
 */
public record TemplateListResponse(
        Long id,
        String templateCode,
        String templateName,
        TemplateStatus status) {
}
