package com.company.xmlgen.template.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request body for {@code PUT /api/v1/templates/{id}/schema}.
 *
 * @see docs/06-api-design/p3_template-api.md §25A
 */
public record UpdateTemplateSchemaRequest(
        Long version,
        @NotNull List<@Valid CreateTemplateFieldRequest> fields,
        @NotNull List<@Valid CreateTemplateMappingRequest> mappings) {
}
