package com.company.xmlgen.template.domain;

/**
 * Business mapping metadata used during schema compilation.
 */
public record TemplateCompileMapping(
        String fieldName,
        String masterDataTypeCode,
        String masterDataFieldName) {
}
