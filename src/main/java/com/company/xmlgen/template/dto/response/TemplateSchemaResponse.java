package com.company.xmlgen.template.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Response {@code data} payload for {@code PUT /api/v1/templates/{id}/schema}.
 *
 * @see docs/06-api-design/p3_template-api.md §25A
 */
public record TemplateSchemaResponse(Long version, List<JsonNode> fields, List<JsonNode> mappings) {
}
