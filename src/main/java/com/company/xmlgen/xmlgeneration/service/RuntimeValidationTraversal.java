package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Shared traversal helpers for runtime validation rules.
 */
final class RuntimeValidationTraversal {

    static final int MAX_TEMPLATE_DEPTH = 20;

    private RuntimeValidationTraversal() {
    }

    static void visitFields(RuntimeTemplate runtimeTemplate, BiConsumer<RuntimeField, Integer> visitor) {
        if (runtimeTemplate == null) {
            return;
        }
        visitFields(runtimeTemplate.roots(), 1, visitor);
    }

    static void visitFields(List<RuntimeField> fields, int depth, BiConsumer<RuntimeField, Integer> visitor) {
        for (RuntimeField field : fields) {
            visitor.accept(field, depth);
            visitFields(field.children(), depth + 1, visitor);
        }
    }

    static List<RuntimeValidationError> collectDuplicateFieldNames(RuntimeTemplate runtimeTemplate) {
        Set<String> seenFieldNames = new HashSet<>();
        List<RuntimeValidationError> errors = new ArrayList<>();
        visitFields(runtimeTemplate, (field, depth) -> {
            if (!seenFieldNames.add(field.fieldName())) {
                errors.add(new RuntimeValidationError(
                        field.fieldName(),
                        HierarchyValidationRule.DUPLICATE_FIELD_NAME,
                        "Duplicate fieldName: " + field.fieldName()));
            }
        });
        return errors;
    }

    static List<RuntimeValidationError> collectDuplicateSiblingDisplayOrders(List<RuntimeField> fields) {
        List<RuntimeValidationError> errors = new ArrayList<>();
        Set<Integer> seenDisplayOrders = new HashSet<>();
        for (RuntimeField field : fields) {
            if (!seenDisplayOrders.add(field.displayOrder())) {
                errors.add(new RuntimeValidationError(
                        field.fieldName(),
                        HierarchyValidationRule.DUPLICATE_DISPLAY_ORDER,
                        "Duplicate displayOrder among sibling nodes: " + field.displayOrder()));
            }
            errors.addAll(collectDuplicateSiblingDisplayOrders(field.children()));
        }
        return errors;
    }

    static List<RuntimeValidationError> collectDepthViolations(RuntimeTemplate runtimeTemplate) {
        List<RuntimeValidationError> errors = new ArrayList<>();
        visitFields(runtimeTemplate, (field, depth) -> {
            if (depth > MAX_TEMPLATE_DEPTH) {
                errors.add(new RuntimeValidationError(
                        field.fieldName(),
                        HierarchyValidationRule.TEMPLATE_DEPTH_EXCEEDED,
                        "Template nesting depth exceeds the allowed maximum depth."));
            }
        });
        return errors;
    }
}
