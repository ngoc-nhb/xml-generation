package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.xmlgeneration.domain.ExecutionNode;
import com.company.xmlgen.xmlgeneration.domain.ExecutionPlan;
import com.company.xmlgen.xmlgeneration.exception.RuntimeExecutionPlannerErrorCode;
import com.company.xmlgen.xmlgeneration.exception.RuntimeExecutionPlannerException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Builds immutable execution plans from runtime template hierarchies.
 */
@Service
public class RuntimeExecutionPlannerImpl implements RuntimeExecutionPlanner {

    @Override
    public ExecutionPlan plan(RuntimeTemplate runtimeTemplate) {
        if (runtimeTemplate == null) {
            throw plannerException(
                    RuntimeExecutionPlannerErrorCode.RUNTIME_TEMPLATE_REQUIRED, "RuntimeTemplate is required");
        }

        List<ExecutionNode> roots = runtimeTemplate.roots().stream()
                .sorted(Comparator.comparingInt(RuntimeField::displayOrder)
                        .thenComparing(RuntimeField::fieldName))
                .map(this::toExecutionNode)
                .toList();
        return new ExecutionPlan(roots);
    }

    private ExecutionNode toExecutionNode(RuntimeField field) {
        List<ExecutionNode> children = field.children().stream()
                .sorted(Comparator.comparingInt(RuntimeField::displayOrder)
                        .thenComparing(RuntimeField::fieldName))
                .map(this::toExecutionNode)
                .toList();

        return new ExecutionNode(
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

    private static RuntimeExecutionPlannerException plannerException(
            RuntimeExecutionPlannerErrorCode errorCode, String message) {
        return new RuntimeExecutionPlannerException(errorCode, message);
    }
}
