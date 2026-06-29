package com.company.xmlgen.template.dto.response;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;

/**
 * Field metadata within a template schema response.
 *
 * @see docs/06-api-design/p3_template-api.md §23
 */
public record TemplateFieldResponse(
        String fieldName,
        String parentFieldName,
        String xmlName,
        String displayName,
        TemplateFieldNodeType nodeType,
        TemplateFieldValueType valueType,
        TemplateFieldSourceType sourceType,
        TemplateFieldOccurrenceRule occurrenceRule,
        TemplateFieldEmptyHandling emptyHandling,
        Boolean requiredWhenParentExists,
        Boolean triggerActivation,
        String defaultValue,
        String staticValue,
        String xmlPath,
        String namespace,
        Integer displayOrder,
        String description) {
}
