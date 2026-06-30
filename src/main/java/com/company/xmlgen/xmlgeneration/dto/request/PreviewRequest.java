package com.company.xmlgen.xmlgeneration.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * HTTP request body for template preview.
 *
 * @see docs/06-api-design/p3_template-api.md §26A
 */
public record PreviewRequest(JsonNode inputData, JsonNode selectedMasterData) {
}
