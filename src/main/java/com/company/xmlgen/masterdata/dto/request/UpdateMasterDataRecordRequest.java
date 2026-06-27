package com.company.xmlgen.masterdata.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code PUT /api/v1/master-data/records/{id}}.
 */
public record UpdateMasterDataRecordRequest(@NotNull JsonNode data) {
}
