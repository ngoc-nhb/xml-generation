package com.company.xmlgen.template.importing.dto.response;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;

/**
 * Imported field metadata within a template draft.
 */
public record TemplateImportDraftFieldResponse(
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
        String description,
        boolean imported) {}
