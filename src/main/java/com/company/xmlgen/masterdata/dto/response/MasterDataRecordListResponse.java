package com.company.xmlgen.masterdata.dto.response;

import java.util.Map;

/**
 * List item payload for {@code GET /api/v1/master-data/records}.
 */
public record MasterDataRecordListResponse(Long id, Long typeId, Map<String, Object> values) {}
