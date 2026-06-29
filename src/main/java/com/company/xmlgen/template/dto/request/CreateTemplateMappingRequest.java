package com.company.xmlgen.template.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Mapping metadata within a create-template schema payload.
 */
public record CreateTemplateMappingRequest(
        @NotBlank String fieldName,
        Long masterDataFieldId) {
}
