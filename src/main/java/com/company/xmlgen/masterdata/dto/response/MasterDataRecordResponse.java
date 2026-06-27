package com.company.xmlgen.masterdata.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Response {@code data} payload for Master Data Record write operations.
 */
public record MasterDataRecordResponse(Long id, Long typeId, JsonNode data) {
}
