package com.company.xmlgen.xmlgeneration.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Resolves request {@code selectedMasterData} record references into field-value scopes
 * used by {@link ValueResolutionService}.
 */
public interface SelectedMasterDataLoader {

    JsonNode load(JsonNode selectedMasterData);
}
