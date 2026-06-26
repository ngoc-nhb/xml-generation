package com.company.xmlgen.masterdata.dto.request;

import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/v1/master-data/types}.
 *
 * @see docs/06-api-design/p4_master-data-api.md §34
 */
public record CreateMasterDataTypeRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull MasterDataTypeStatus status) {
}
