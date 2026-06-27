package com.company.xmlgen.template.dto.request;

import com.company.xmlgen.template.dto.response.TemplateSchemaResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/v1/templates}.
 *
 * @see docs/06-api-design/p3_template-api.md §24
 */
public record CreateTemplateRequest(
        @NotBlank @Size(max = 100) String code,
        @NotBlank @Size(max = 255) String name,
        String description,
        TemplateSchemaResponse schema) {
}
