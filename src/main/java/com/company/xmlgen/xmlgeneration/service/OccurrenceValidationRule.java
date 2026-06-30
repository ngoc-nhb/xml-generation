package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Validates runtime occurrence rule configuration.
 */
@Component
public class OccurrenceValidationRule implements RuntimeValidationRule {

    static final String OCCURRENCE_RULE_REQUIRED = "OCCURRENCE_RULE_REQUIRED";
    static final String OCCURRENCE_RULE_NOT_ALLOWED = "OCCURRENCE_RULE_NOT_ALLOWED";

    @Override
    public int priority() {
        return 300;
    }

    @Override
    public List<RuntimeValidationError> validate(RuntimeValidationContext context) {
        List<RuntimeValidationError> errors = new ArrayList<>();
        RuntimeValidationTraversal.visitFields(context.runtimeTemplate(), (field, depth) -> errors.addAll(validateField(field)));
        return errors;
    }

    private static List<RuntimeValidationError> validateField(RuntimeField field) {
        List<RuntimeValidationError> errors = new ArrayList<>();
        if (field.nodeType() == TemplateFieldNodeType.GROUP) {
            if (field.occurrenceRule() == null) {
                errors.add(new RuntimeValidationError(
                        field.fieldName(),
                        OCCURRENCE_RULE_REQUIRED,
                        "occurrenceRule is required for GROUP nodes."));
            }
            return errors;
        }

        if (field.occurrenceRule() != null) {
            errors.add(new RuntimeValidationError(
                    field.fieldName(),
                    OCCURRENCE_RULE_NOT_ALLOWED,
                    "occurrenceRule is allowed only on GROUP nodes."));
        }
        return errors;
    }
}
