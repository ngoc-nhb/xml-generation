package com.company.xmlgen.template.domain;

import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import java.util.List;

/**
 * Repairs runtime fields whose persisted metadata does not match structural hierarchy rules.
 * Parent nodes with children must be GROUP nodes without source or value configuration.
 */
public final class RuntimeFieldNormalizer {

    private RuntimeFieldNormalizer() {}

    public static RuntimeField normalizeContainer(RuntimeField field) {
        List<RuntimeField> children = field.children().stream()
                .map(RuntimeFieldNormalizer::normalizeContainer)
                .toList();

        if (!children.isEmpty()) {
            TemplateFieldOccurrenceRule occurrenceRule = field.occurrenceRule() != null
                    ? field.occurrenceRule()
                    : TemplateFieldOccurrenceRule.ONE_OR_MORE;
            return new RuntimeField(
                    field.fieldName(),
                    field.xmlName(),
                    field.displayName(),
                    TemplateFieldNodeType.GROUP,
                    null,
                    null,
                    occurrenceRule,
                    field.emptyHandling(),
                    field.requiredWhenParentExists(),
                    field.triggerActivation(),
                    field.defaultValue(),
                    null,
                    field.xmlPath(),
                    field.namespace(),
                    field.displayOrder(),
                    field.description(),
                    children);
        }

        if (children == field.children()) {
            return field;
        }

        return new RuntimeField(
                field.fieldName(),
                field.xmlName(),
                field.displayName(),
                field.nodeType(),
                field.valueType(),
                field.sourceType(),
                field.occurrenceRule(),
                field.emptyHandling(),
                field.requiredWhenParentExists(),
                field.triggerActivation(),
                field.defaultValue(),
                field.staticValue(),
                field.xmlPath(),
                field.namespace(),
                field.displayOrder(),
                field.description(),
                children);
    }
}
