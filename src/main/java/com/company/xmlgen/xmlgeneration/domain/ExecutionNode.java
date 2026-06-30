package com.company.xmlgen.xmlgeneration.domain;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import java.util.List;

/**
 * Immutable execution node used by value resolution and XML generation.
 */
public record ExecutionNode(
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
        List<ExecutionNode> children) {

    public ExecutionNode {
        children = List.copyOf(children);
    }
}
