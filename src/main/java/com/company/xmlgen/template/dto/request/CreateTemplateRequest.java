package com.company.xmlgen.template.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/v1/templates}.
 *
 * @see docs/06-api-design/p3_template-api.md §24
 */
public record CreateTemplateRequest(
        @NotBlank @Size(max = 100) String templateCode,
        @NotBlank @Size(max = 255) String templateName,
        String description) {
}
