package com.company.xmlgen.masterdata.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/v1/master-data/records}.
 */
public record CreateMasterDataRecordRequest(@NotNull Long typeId, @NotNull JsonNode data) {
}
