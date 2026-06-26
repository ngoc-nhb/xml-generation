package com.company.xmlgen.masterdata.dto.request;

import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code PUT /api/v1/master-data/types/{id}}.
 *
 * @see docs/06-api-design/p4_master-data-api.md §35
 */
public record UpdateMasterDataTypeRequest(
        @NotBlank String name, String description, @NotNull MasterDataTypeStatus status) {
}
