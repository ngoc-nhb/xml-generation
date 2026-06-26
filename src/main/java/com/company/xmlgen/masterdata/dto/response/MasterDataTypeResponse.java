package com.company.xmlgen.masterdata.dto.response;

import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;

/**
 * Response {@code data} payload for {@code GET /api/v1/master-data/types/{id}}.
 *
 * @see docs/06-api-design/p4_master-data-api.md §33
 */
public record MasterDataTypeResponse(
        Long id, String code, String name, MasterDataTypeStatus status) {
}
