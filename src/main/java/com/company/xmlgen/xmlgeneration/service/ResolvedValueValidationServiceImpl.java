package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionNode;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;
import com.company.xmlgen.xmlgeneration.exception.RuntimeValidationErrorCode;
import com.company.xmlgen.xmlgeneration.exception.RuntimeValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Validates resolved execution values. Only {@link TemplateFieldEmptyHandling#REQUIRED}
 * fields with empty resolved values are rejected; other empty-handling strategies are
 * applied during XML serialization.
 */
@Service
public class ResolvedValueValidationServiceImpl implements ResolvedValueValidationService {

    static final String REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING";
    private static final String REQUIRED_FIELD_MESSAGE = "Required field is missing.";

    @Override
    public RuntimeValidationResult validate(RuntimeExecutionTree executionTree) {
        if (executionTree == null) {
            throw new RuntimeValidationException(
                    RuntimeValidationErrorCode.EXECUTION_TREE_REQUIRED, "RuntimeExecutionTree is required");
        }

        List<RuntimeValidationError> errors = new ArrayList<>();
        visitNodes(executionTree.roots(), errors);
        if (errors.isEmpty()) {
            return RuntimeValidationResult.valid();
        }
        return RuntimeValidationResult.invalid(errors);
    }

    private static void visitNodes(List<RuntimeExecutionNode> nodes, List<RuntimeValidationError> errors) {
        for (RuntimeExecutionNode node : nodes) {
            validateNode(node, errors);
            visitNodes(node.children(), errors);
        }
    }

    private static void validateNode(RuntimeExecutionNode node, List<RuntimeValidationError> errors) {
        if (node.field().nodeType() == TemplateFieldNodeType.GROUP) {
            return;
        }
        if (node.field().emptyHandling() != TemplateFieldEmptyHandling.REQUIRED) {
            return;
        }
        if (isEmptyValue(node.value())) {
            errors.add(new RuntimeValidationError(
                    node.field().fieldName(), REQUIRED_FIELD_MISSING, REQUIRED_FIELD_MESSAGE));
        }
    }

    private static boolean isEmptyValue(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return true;
        }
        return value.isTextual() && value.asText().isBlank();
    }
}
