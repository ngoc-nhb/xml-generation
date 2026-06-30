package com.company.xmlgen.xmlgeneration.dto.request;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * HTTP request body for template export.
 *
 * @see docs/06-api-design/p3_template-api.md §26B
 */
public record ExportRequest(JsonNode inputData, JsonNode selectedMasterData) {
}
