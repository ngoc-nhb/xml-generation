package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Validates runtime hierarchy constraints.
 */
@Component
public class HierarchyValidationRule implements RuntimeValidationRule {

    static final String DUPLICATE_FIELD_NAME = "DUPLICATE_FIELD_NAME";
    static final String DUPLICATE_DISPLAY_ORDER = "DUPLICATE_DISPLAY_ORDER";
    static final String TEMPLATE_DEPTH_EXCEEDED = "TEMPLATE_DEPTH_EXCEEDED";

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public List<RuntimeValidationError> validate(RuntimeValidationContext context) {
        RuntimeTemplate runtimeTemplate = context.runtimeTemplate();
        List<RuntimeValidationError> errors = new ArrayList<>();
        errors.addAll(RuntimeValidationTraversal.collectDuplicateFieldNames(runtimeTemplate));
        errors.addAll(RuntimeValidationTraversal.collectDuplicateSiblingDisplayOrders(runtimeTemplate.roots()));
        errors.addAll(RuntimeValidationTraversal.collectDepthViolations(runtimeTemplate));
        return errors;
    }
}
