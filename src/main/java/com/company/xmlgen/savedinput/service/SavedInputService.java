package com.company.xmlgen.savedinput.service;

import com.company.xmlgen.savedinput.dto.response.SavedInputResponse;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Manages user Saved Input drafts for XML Generation.
 */
public interface SavedInputService {

    SavedInputResponse findByTemplate(Long templateId);

    void saveFromExport(Long templateId, JsonNode inputData, JsonNode selectedMasterData);
}
