package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Validates runtime node type and source configuration.
 */
@Component
public class NodeTypeValidationRule implements RuntimeValidationRule {

    static final String SOURCE_TYPE_REQUIRED = "SOURCE_TYPE_REQUIRED";
    static final String VALUE_TYPE_REQUIRED = "VALUE_TYPE_REQUIRED";
    static final String GROUP_SOURCE_TYPE_NOT_ALLOWED = "GROUP_SOURCE_TYPE_NOT_ALLOWED";
    static final String GROUP_VALUE_TYPE_NOT_ALLOWED = "GROUP_VALUE_TYPE_NOT_ALLOWED";
    static final String ATTRIBUTE_CHILDREN_NOT_ALLOWED = "ATTRIBUTE_CHILDREN_NOT_ALLOWED";
    static final String STATIC_VALUE_REQUIRED = "STATIC_VALUE_REQUIRED";

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public List<RuntimeValidationError> validate(RuntimeValidationContext context) {
        List<RuntimeValidationError> errors = new ArrayList<>();
        RuntimeValidationTraversal.visitFields(context.runtimeTemplate(), (field, depth) -> errors.addAll(validateField(field)));
        return errors;
    }

    private static List<RuntimeValidationError> validateField(RuntimeField field) {
        List<RuntimeValidationError> errors = new ArrayList<>();
        TemplateFieldNodeType nodeType = field.nodeType();

        if (nodeType == TemplateFieldNodeType.GROUP) {
            if (field.sourceType() != null) {
                errors.add(error(field.fieldName(), GROUP_SOURCE_TYPE_NOT_ALLOWED, "GROUP nodes must not define sourceType."));
            }
            if (field.valueType() != null) {
                errors.add(error(field.fieldName(), GROUP_VALUE_TYPE_NOT_ALLOWED, "GROUP nodes must not define valueType."));
            }
            return errors;
        }

        if (field.sourceType() == null) {
            errors.add(error(field.fieldName(), SOURCE_TYPE_REQUIRED, "sourceType is required for value nodes."));
        }
        if (field.valueType() == null) {
            errors.add(error(field.fieldName(), VALUE_TYPE_REQUIRED, "valueType is required for value nodes."));
        }
        if (nodeType == TemplateFieldNodeType.ATTRIBUTE && !field.children().isEmpty()) {
            errors.add(error(
                    field.fieldName(),
                    ATTRIBUTE_CHILDREN_NOT_ALLOWED,
                    "ATTRIBUTE nodes must not contain child nodes."));
        }
        if (field.sourceType() == TemplateFieldSourceType.STATIC && isBlank(field.staticValue())) {
            errors.add(error(field.fieldName(), STATIC_VALUE_REQUIRED, "staticValue is required when sourceType is STATIC."));
        }
        return errors;
    }

    private static RuntimeValidationError error(String fieldName, String code, String message) {
        return new RuntimeValidationError(fieldName, code, message);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
