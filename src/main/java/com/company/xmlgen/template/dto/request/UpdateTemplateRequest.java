package com.company.xmlgen.template.dto.request;

import com.company.xmlgen.template.entity.TemplateStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code PUT /api/v1/templates/{id}}.
 *
 * @see docs/06-api-design/p3_template-api.md §25
 */
public record UpdateTemplateRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotNull TemplateStatus status) {
}
