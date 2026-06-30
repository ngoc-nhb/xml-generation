package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionNode;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;
import com.company.xmlgen.xmlgeneration.exception.ValueResolutionErrorCode;
import com.company.xmlgen.xmlgeneration.exception.ValueResolutionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Resolves runtime template fields into immutable execution values.
 *
 * <p>Resolution precedence for {@code sourceType = INPUT}:
 *
 * <ol>
 *   <li>value from the current input scope at {@code fieldName}
 *   <li>if absent or empty, {@code RuntimeField.defaultValue}
 * </ol>
 *
 * <p>{@code sourceType = STATIC} resolves {@code RuntimeField.staticValue}.
 *
 * <p>{@code sourceType = MASTER_DATA} resolves from {@code selectedMasterData} using mapping
 * metadata supplied in the execution context.
 */
@Service
public class ValueResolutionServiceImpl implements ValueResolutionService {

    @Override
    public RuntimeExecutionTree resolve(RuntimeTemplate runtimeTemplate, ValueResolutionContext context) {
        if (runtimeTemplate == null) {
            throw resolutionException(
                    ValueResolutionErrorCode.RUNTIME_TEMPLATE_REQUIRED, "RuntimeTemplate is required");
        }
        if (context == null) {
            throw resolutionException(
                    ValueResolutionErrorCode.RESOLUTION_CONTEXT_REQUIRED, "ValueResolutionContext is required");
        }

        JsonNode inputScope = nullSafe(context.inputData());
        JsonNode masterDataScope = nullSafe(context.selectedMasterData());
        Map<String, TemplateCompileMapping> mappingsByFieldName = context.mappingsByFieldName();

        List<RuntimeExecutionNode> roots = new ArrayList<>();
        for (RuntimeField rootField : sortedFields(runtimeTemplate.roots())) {
            roots.addAll(resolveFieldOccurrences(rootField, inputScope, masterDataScope, mappingsByFieldName, true));
        }
        return new RuntimeExecutionTree(roots);
    }

    private List<RuntimeExecutionNode> resolveFieldOccurrences(
            RuntimeField field,
            JsonNode inputScope,
            JsonNode masterDataScope,
            Map<String, TemplateCompileMapping> mappingsByFieldName,
            boolean rootGroup) {
        if (field.nodeType() != TemplateFieldNodeType.GROUP) {
            return List.of(resolveField(field, inputScope, masterDataScope, mappingsByFieldName));
        }

        if (rootGroup) {
            return List.of(resolveField(field, inputScope, masterDataScope, mappingsByFieldName));
        }

        JsonNode groupInput = inputScope.get(field.fieldName());
        JsonNode groupMasterData = masterDataScope.get(field.fieldName());

        if (!isRepeatable(field.occurrenceRule())) {
            if (field.occurrenceRule() == TemplateFieldOccurrenceRule.ZERO_OR_ONE && isMissing(groupInput)) {
                return List.of();
            }
            return List.of(resolveField(
                    field, nullSafe(groupInput), nullSafe(groupMasterData), mappingsByFieldName));
        }

        if (isMissing(groupInput)) {
            return List.of();
        }
        if (groupInput.isArray()) {
            List<RuntimeExecutionNode> occurrences = new ArrayList<>();
            JsonNode masterOccurrences = nullSafe(groupMasterData);
            for (int index = 0; index < groupInput.size(); index++) {
                JsonNode occurrenceInput = groupInput.get(index);
                JsonNode occurrenceMasterData =
                        masterOccurrences.isArray() && index < masterOccurrences.size()
                                ? masterOccurrences.get(index)
                                : NullNode.instance;
                occurrences.add(resolveField(
                        field, occurrenceInput, occurrenceMasterData, mappingsByFieldName));
            }
            return occurrences;
        }

        return List.of(resolveField(field, groupInput, nullSafe(groupMasterData), mappingsByFieldName));
    }

    private RuntimeExecutionNode resolveField(
            RuntimeField field,
            JsonNode inputScope,
            JsonNode masterDataScope,
            Map<String, TemplateCompileMapping> mappingsByFieldName) {
        JsonNode resolvedValue = resolveValue(field, inputScope, masterDataScope, mappingsByFieldName);
        List<RuntimeExecutionNode> children = resolveChildren(field, inputScope, masterDataScope, mappingsByFieldName);
        return new RuntimeExecutionNode(field, resolvedValue, children);
    }

    private List<RuntimeExecutionNode> resolveChildren(
            RuntimeField field,
            JsonNode inputScope,
            JsonNode masterDataScope,
            Map<String, TemplateCompileMapping> mappingsByFieldName) {
        if (field.nodeType() != TemplateFieldNodeType.GROUP) {
            return List.of();
        }

        List<RuntimeExecutionNode> children = new ArrayList<>();
        for (RuntimeField child : sortedFields(field.children())) {
            children.addAll(
                    resolveFieldOccurrences(child, inputScope, masterDataScope, mappingsByFieldName, false));
        }
        return children;
    }

    private static JsonNode resolveValue(
            RuntimeField field,
            JsonNode inputScope,
            JsonNode masterDataScope,
            Map<String, TemplateCompileMapping> mappingsByFieldName) {
        if (field.nodeType() == TemplateFieldNodeType.GROUP) {
            return NullNode.instance;
        }

        TemplateFieldSourceType sourceType = field.sourceType();
        if (sourceType == TemplateFieldSourceType.STATIC) {
            return toJsonValue(field.staticValue());
        }
        if (sourceType == TemplateFieldSourceType.MASTER_DATA) {
            return resolveMasterDataValue(field, masterDataScope, mappingsByFieldName);
        }
        return resolveInputValue(field, inputScope);
    }

    private static JsonNode resolveInputValue(RuntimeField field, JsonNode inputScope) {
        JsonNode inputValue = inputScope.get(field.fieldName());
        if (!isEmpty(inputValue)) {
            return inputValue;
        }
        return toJsonValue(field.defaultValue());
    }

    private static JsonNode resolveMasterDataValue(
            RuntimeField field,
            JsonNode masterDataScope,
            Map<String, TemplateCompileMapping> mappingsByFieldName) {
        TemplateCompileMapping mapping = mappingsByFieldName.get(field.fieldName());
        if (mapping == null) {
            return NullNode.instance;
        }

        JsonNode masterDataTypeNode = masterDataScope.get(mapping.masterDataTypeCode());
        if (isMissing(masterDataTypeNode)) {
            return NullNode.instance;
        }

        JsonNode masterDataFieldNode = masterDataTypeNode.get(mapping.masterDataFieldName());
        return isMissing(masterDataFieldNode) ? NullNode.instance : masterDataFieldNode;
    }

    private static boolean isRepeatable(TemplateFieldOccurrenceRule occurrenceRule) {
        return occurrenceRule == TemplateFieldOccurrenceRule.ZERO_OR_MORE
                || occurrenceRule == TemplateFieldOccurrenceRule.ONE_OR_MORE;
    }

    private static List<RuntimeField> sortedFields(List<RuntimeField> fields) {
        return fields.stream()
                .sorted(Comparator.comparingInt(RuntimeField::displayOrder)
                        .thenComparing(RuntimeField::fieldName))
                .toList();
    }

    private static JsonNode nullSafe(JsonNode node) {
        return node == null || node.isNull() ? NullNode.instance : node;
    }

    private static boolean isMissing(JsonNode node) {
        return node == null || node.isNull() || node.isMissingNode();
    }

    private static boolean isEmpty(JsonNode node) {
        if (isMissing(node)) {
            return true;
        }
        return node.isTextual() && node.asText().isBlank();
    }

    private static JsonNode toJsonValue(String value) {
        if (value == null) {
            return NullNode.instance;
        }
        return TextNode.valueOf(value);
    }

    private static ValueResolutionException resolutionException(
            ValueResolutionErrorCode errorCode, String message) {
        return new ValueResolutionException(errorCode, message);
    }
}
