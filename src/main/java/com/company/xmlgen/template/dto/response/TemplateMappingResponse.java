package com.company.xmlgen.template.dto.response;

/**
 * Mapping metadata within a template schema response.
 *
 * @see docs/06-api-design/p3_template-api.md §23
 */
public record TemplateMappingResponse(String fieldName, Long masterDataFieldId) {
}
