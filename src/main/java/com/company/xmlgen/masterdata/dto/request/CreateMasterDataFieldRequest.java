package com.company.xmlgen.masterdata.dto.request;

import com.company.xmlgen.masterdata.entity.MasterDataFieldDataType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/v1/master-data/fields}.
 */
public record CreateMasterDataFieldRequest(
        @NotNull Long typeId,
        @NotBlank String code,
        @NotBlank String name,
        @NotNull MasterDataFieldDataType dataType,
        @NotNull Boolean required,
        @NotNull @Min(1) Integer displayOrder,
        String description,
        String defaultValue,
        Boolean unique,
        Boolean searchable,
        Long masterDataReferenceTypeId) {
}
