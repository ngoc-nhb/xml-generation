package com.company.xmlgen.masterdata.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

/**
 * Response {@code data} payload for {@code GET /api/v1/master-data/records/{id}}.
 */
public record MasterDataRecordDetailResponse(
        Long id,
        Long typeId,
        JsonNode data,
        Instant createdAt,
        Instant updatedAt) {
}
