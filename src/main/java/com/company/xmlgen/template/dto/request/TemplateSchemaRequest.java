package com.company.xmlgen.template.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request body for {@code PUT /api/v1/templates/{id}/schema}.
 *
 * @see docs/06-api-design/p3_template-api.md §25A
 */
public record TemplateSchemaRequest(
        @NotNull Long version,
        @NotNull List<JsonNode> fields,
        @NotNull List<JsonNode> mappings) {
}
