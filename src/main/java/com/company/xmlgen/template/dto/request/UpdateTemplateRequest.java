package com.company.xmlgen.template.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code PUT /api/v1/templates/{id}}.
 *
 * @see docs/06-api-design/p3_template-api.md §25
 */
public record UpdateTemplateRequest(
        @NotBlank @Size(max = 255) String templateName,
        String description) {
}
