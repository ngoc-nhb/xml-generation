package com.company.xmlgen.masterdata.dto.response;

import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;

/**
 * Response {@code data} payload for {@code POST /api/v1/master-data/types}.
 *
 * @see docs/06-api-design/p4_master-data-api.md §34
 */
public record CreateMasterDataTypeResponse(
        Long id, String code, String name, String description, MasterDataTypeStatus status) {
}
