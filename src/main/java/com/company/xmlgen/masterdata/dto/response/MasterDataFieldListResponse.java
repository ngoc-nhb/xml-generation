package com.company.xmlgen.masterdata.dto.response;

import com.company.xmlgen.masterdata.entity.MasterDataFieldDataType;

/**
 * List item {@code data} payload for {@code GET /api/v1/master-data/fields}.
 */
public record MasterDataFieldListResponse(
        Long id,
        Long typeId,
        String typeCode,
        String typeName,
        String code,
        String name,
        MasterDataFieldDataType dataType,
        boolean required,
        int displayOrder,
        String description,
        String defaultValue,
        boolean unique,
        boolean searchable,
        Long masterDataReferenceTypeId) {
}
