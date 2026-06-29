package com.company.xmlgen.template.dto.response;

import java.util.List;

/**
 * Editable template schema reconstructed from metadata.
 *
 * @see docs/06-api-design/p3_template-api.md §23
 */
public record TemplateSchemaResponse(
        Long version,
        List<TemplateFieldResponse> fields,
        List<TemplateMappingResponse> mappings) {
}
