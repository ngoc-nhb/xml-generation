package com.company.xmlgen.xmlgeneration.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Application request for XML preview.
 */
public record PreviewRequest(Long templateId, JsonNode inputData, JsonNode selectedMasterData) {
}
