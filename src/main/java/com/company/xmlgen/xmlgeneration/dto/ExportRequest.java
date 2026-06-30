package com.company.xmlgen.xmlgeneration.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Application request for XML export.
 */
public record ExportRequest(Long templateId, JsonNode inputData, JsonNode selectedMasterData) {
}
