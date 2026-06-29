package com.company.xmlgen.template.dto.request;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Optional editable schema payload for {@code POST /api/v1/templates}.
 */
public record CreateTemplateSchemaRequest(
        List<@Valid CreateTemplateFieldRequest> fields,
        List<@Valid CreateTemplateMappingRequest> mappings) {
}
