package com.company.xmlgen.template.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.exception.TemplateSchemaParserErrorCode;
import com.company.xmlgen.template.exception.TemplateSchemaParserException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Converts Template metadata into an in-memory runtime schema model.
 */
@Service
public class TemplateSchemaParserImpl implements TemplateSchemaParser {

    @Override
    public RuntimeTemplate parse(
            TemplateEntity template,
            List<TemplateFieldEntity> fields) {
        if (template == null) {
            throw parserException(
                    TemplateSchemaParserErrorCode.TEMPLATE_SCHEMA_INVALID,
                    "Template is required");
        }

        List<TemplateFieldEntity> safeFields = fields == null ? List.of() : fields;

        Map<Long, TemplateFieldEntity> fieldsById = indexFieldsById(safeFields);
        validateUniqueFieldNames(safeFields);
        validateParents(safeFields, fieldsById);
        validateNoCycles(safeFields);

        Map<Long, List<TemplateFieldEntity>> childrenByParentId = groupChildrenByParentId(safeFields);

        List<RuntimeField> roots = sorted(safeFields.stream()
                        .filter(field -> field.getParentId() == null)
                        .toList())
                .stream()
                .map(field -> toRuntimeField(field, childrenByParentId))
                .toList();

        return new RuntimeTemplate(roots);
    }

    private static Map<Long, TemplateFieldEntity> indexFieldsById(List<TemplateFieldEntity> fields) {
        Map<Long, TemplateFieldEntity> fieldsById = new HashMap<>();
        for (TemplateFieldEntity field : fields) {
            if (field.getId() == null) {
                throw parserException(
                        TemplateSchemaParserErrorCode.TEMPLATE_SCHEMA_INVALID,
                        "TemplateField id is required");
            }
            TemplateFieldEntity previous = fieldsById.put(field.getId(), field);
            if (previous != null) {
                throw parserException(
                        TemplateSchemaParserErrorCode.TEMPLATE_SCHEMA_INVALID,
                        "Duplicate TemplateField id: " + field.getId());
            }
        }
        return fieldsById;
    }

    private static void validateUniqueFieldNames(List<TemplateFieldEntity> fields) {
        Set<String> fieldNames = new HashSet<>();
        for (TemplateFieldEntity field : fields) {
            if (!fieldNames.add(field.getFieldName())) {
                throw parserException(
                        TemplateSchemaParserErrorCode.TEMPLATE_FIELD_NAME_DUPLICATE,
                        "Duplicate fieldName: " + field.getFieldName());
            }
        }
    }

    private static void validateParents(
            List<TemplateFieldEntity> fields, Map<Long, TemplateFieldEntity> fieldsById) {
        for (TemplateFieldEntity field : fields) {
            if (field.getParentId() == null) {
                continue;
            }
            if (!fieldsById.containsKey(field.getParentId())) {
                throw parserException(
                        TemplateSchemaParserErrorCode.TEMPLATE_PARENT_FIELD_NOT_FOUND,
                        "Missing parent for fieldName: " + field.getFieldName());
            }
        }
    }

    private static void validateNoCycles(List<TemplateFieldEntity> fields) {
        Map<Long, Long> parentByFieldId = new HashMap<>();
        for (TemplateFieldEntity field : fields) {
            parentByFieldId.put(field.getId(), field.getParentId());
        }

        Set<Long> visited = new HashSet<>();
        for (TemplateFieldEntity field : fields) {
            detectCycle(field.getId(), parentByFieldId, visited, new HashSet<>());
        }
    }

    private static void detectCycle(
            Long fieldId, Map<Long, Long> parentByFieldId, Set<Long> visited, Set<Long> visiting) {
        if (fieldId == null || visited.contains(fieldId)) {
            return;
        }
        if (!visiting.add(fieldId)) {
            throw parserException(
                    TemplateSchemaParserErrorCode.TEMPLATE_PARENT_CYCLE,
                    "Circular parent relationship detected");
        }

        detectCycle(parentByFieldId.get(fieldId), parentByFieldId, visited, visiting);
        visiting.remove(fieldId);
        visited.add(fieldId);
    }

    private static Map<Long, List<TemplateFieldEntity>> groupChildrenByParentId(
            List<TemplateFieldEntity> fields) {
        Map<Long, List<TemplateFieldEntity>> childrenByParentId = new HashMap<>();
        for (TemplateFieldEntity field : fields) {
            if (field.getParentId() == null) {
                continue;
            }
            childrenByParentId.computeIfAbsent(field.getParentId(), ignored -> new ArrayList<>())
                    .add(field);
        }
        childrenByParentId.replaceAll((ignored, children) -> sorted(children));
        return childrenByParentId;
    }

    private static RuntimeField toRuntimeField(
            TemplateFieldEntity field,
            Map<Long, List<TemplateFieldEntity>> childrenByParentId) {
        List<RuntimeField> children = childrenByParentId
                .getOrDefault(field.getId(), List.of())
                .stream()
                .map(child -> toRuntimeField(child, childrenByParentId))
                .toList();

        return new RuntimeField(
                field.getFieldName(),
                field.getXmlName(),
                field.getDisplayName(),
                field.getNodeType(),
                field.getValueType(),
                field.getSourceType(),
                field.getOccurrenceRule(),
                field.getEmptyHandling(),
                field.isRequiredWhenParentExists(),
                field.getTriggerActivation(),
                field.getDefaultValue(),
                field.getStaticValue(),
                field.getXmlPath(),
                field.getNamespace(),
                field.getDisplayOrder(),
                field.getDescription(),
                children);
    }

    private static List<TemplateFieldEntity> sorted(List<TemplateFieldEntity> fields) {
        return fields.stream()
                .sorted(Comparator.comparingInt(TemplateFieldEntity::getDisplayOrder)
                        .thenComparing(TemplateFieldEntity::getFieldName))
                .toList();
    }

    private static TemplateSchemaParserException parserException(
            TemplateSchemaParserErrorCode errorCode, String message) {
        return new TemplateSchemaParserException(errorCode, message);
    }
}
