package com.company.xmlgen.template.domain;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import java.util.List;

/**
 * Runtime representation of one XML schema field.
 */
public record RuntimeField(
        String fieldName,
        String xmlName,
        String displayName,
        TemplateFieldNodeType nodeType,
        TemplateFieldValueType valueType,
        TemplateFieldSourceType sourceType,
        TemplateFieldOccurrenceRule occurrenceRule,
        TemplateFieldEmptyHandling emptyHandling,
        boolean requiredWhenParentExists,
        Boolean triggerActivation,
        String defaultValue,
        String staticValue,
        String xmlPath,
        String namespace,
        int displayOrder,
        String description,
        List<RuntimeField> children) {

    public RuntimeField {
        children = List.copyOf(children);
    }
}
