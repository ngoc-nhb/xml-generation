package com.company.xmlgen.masterdata.dto.response;

import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;

/**
 * Response {@code data} payload for {@code PUT /api/v1/master-data/types/{id}}.
 *
 * @see docs/06-api-design/p4_master-data-api.md §35
 */
public record UpdateMasterDataTypeResponse(
        Long id, String code, String name, String description, MasterDataTypeStatus status) {
}
