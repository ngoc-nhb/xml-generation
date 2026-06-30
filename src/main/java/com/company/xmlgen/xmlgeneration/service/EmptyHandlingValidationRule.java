package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
/**
 * Validates empty-handling configuration against supported value types.
 *
 * <p>This check belongs at template compile time. It is not registered as a runtime
 * validation rule because empty-handling is a serialization policy, not an input rule.
 */
public class EmptyHandlingValidationRule implements RuntimeValidationRule {

    static final String INVALID_EMPTY_HANDLING = "INVALID_EMPTY_HANDLING";

    private static final Map<TemplateFieldValueType, EnumSet<TemplateFieldEmptyHandling>> ALLOWED_EMPTY_HANDLING =
            Map.of(
                    TemplateFieldValueType.STRING,
                    EnumSet.of(
                            TemplateFieldEmptyHandling.REQUIRED,
                            TemplateFieldEmptyHandling.OMIT_IF_EMPTY,
                            TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY),
                    TemplateFieldValueType.INTEGER,
                    EnumSet.of(
                            TemplateFieldEmptyHandling.REQUIRED,
                            TemplateFieldEmptyHandling.OMIT_IF_EMPTY,
                            TemplateFieldEmptyHandling.ZERO_IF_EMPTY),
                    TemplateFieldValueType.LONG,
                    EnumSet.of(
                            TemplateFieldEmptyHandling.REQUIRED,
                            TemplateFieldEmptyHandling.OMIT_IF_EMPTY,
                            TemplateFieldEmptyHandling.ZERO_IF_EMPTY),
                    TemplateFieldValueType.DECIMAL,
                    EnumSet.of(
                            TemplateFieldEmptyHandling.REQUIRED,
                            TemplateFieldEmptyHandling.OMIT_IF_EMPTY,
                            TemplateFieldEmptyHandling.ZERO_IF_EMPTY),
                    TemplateFieldValueType.BOOLEAN,
                    EnumSet.of(TemplateFieldEmptyHandling.REQUIRED, TemplateFieldEmptyHandling.OMIT_IF_EMPTY),
                    TemplateFieldValueType.DATE,
                    EnumSet.of(TemplateFieldEmptyHandling.REQUIRED, TemplateFieldEmptyHandling.OMIT_IF_EMPTY),
                    TemplateFieldValueType.DATETIME,
                    EnumSet.of(TemplateFieldEmptyHandling.REQUIRED, TemplateFieldEmptyHandling.OMIT_IF_EMPTY));

    @Override
    public int priority() {
        return 400;
    }

    @Override
    public List<RuntimeValidationError> validate(RuntimeValidationContext context) {
        List<RuntimeValidationError> errors = new ArrayList<>();
        RuntimeValidationTraversal.visitFields(context.runtimeTemplate(), (field, depth) -> errors.addAll(validateField(field)));
        return errors;
    }

    private static List<RuntimeValidationError> validateField(RuntimeField field) {
        if (field.nodeType() == TemplateFieldNodeType.GROUP || field.valueType() == null) {
            return List.of();
        }

        EnumSet<TemplateFieldEmptyHandling> allowed = ALLOWED_EMPTY_HANDLING.get(field.valueType());
        if (allowed != null && !allowed.contains(field.emptyHandling())) {
            return List.of(new RuntimeValidationError(
                    field.fieldName(),
                    INVALID_EMPTY_HANDLING,
                    "emptyHandling " + field.emptyHandling() + " is not allowed for valueType "
                            + field.valueType() + "."));
        }
        return List.of();
    }
}
