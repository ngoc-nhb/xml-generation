package com.company.xmlgen.savedinput.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

/**
 * Saved Input payload returned to clients.
 */
public record SavedInputResponse(
        Long templateId,
        JsonNode inputData,
        JsonNode selectedMasterData,
        Instant updatedAt) {}
