package com.company.xmlgen.template.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Response {@code data} payload for template schema APIs.
 *
 * @see docs/06-api-design/p3_template-api.md §25A
 */
public record TemplateSchemaResponse(Long version, List<JsonNode> fields, List<JsonNode> mappings) {
}
