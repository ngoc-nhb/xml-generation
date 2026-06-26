package com.company.xmlgen.masterdata.dto.response;

import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;

/**
 * List item payload for {@code GET /api/v1/master-data/types}.
 *
 * @see docs/06-api-design/p4_master-data-api.md §32
 */
public record MasterDataTypeListResponse(
        Long id, String code, String name, MasterDataTypeStatus status) {
}
