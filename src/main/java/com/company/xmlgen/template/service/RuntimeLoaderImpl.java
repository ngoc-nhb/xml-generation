package com.company.xmlgen.template.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import com.company.xmlgen.template.exception.RuntimeLoaderErrorCode;
import com.company.xmlgen.template.exception.RuntimeLoaderException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Deserializes compiled schema JSON into the shared runtime template model.
 */
@Service
public class RuntimeLoaderImpl implements RuntimeLoader {

    @Override
    public RuntimeTemplate load(JsonNode compiledSchemaJson) {
        if (compiledSchemaJson == null || compiledSchemaJson.isNull()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_INVALID, "Compiled schema JSON is required");
        }

        JsonNode rootsNode = compiledSchemaJson.get("roots");
        if (rootsNode == null || rootsNode.isNull()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_INVALID, "Compiled schema roots are required");
        }
        if (!rootsNode.isArray()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_INVALID, "Compiled schema roots must be an array");
        }

        List<RuntimeField> roots = loadFields(rootsNode);
        return new RuntimeTemplate(roots);
    }

    private List<RuntimeField> loadFields(JsonNode nodes) {
        List<RuntimeField> fields = new ArrayList<>();
        for (JsonNode node : nodes) {
            if (!node.isObject()) {
                throw loaderException(
                        RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                        "Compiled schema field node must be an object");
            }
            fields.add(loadField(node));
        }
        return sortFields(fields);
    }

    private RuntimeField loadField(JsonNode node) {
        String fieldName = requiredText(node, "fieldName");
        String xmlName = requiredText(node, "name");
        TemplateFieldNodeType nodeType = requiredEnum(node, "fieldType", TemplateFieldNodeType.class);
        TemplateFieldEmptyHandling emptyHandling = requiredEnum(node, "emptyHandling", TemplateFieldEmptyHandling.class);
        int displayOrder = requiredInt(node, "displayOrder");

        return new RuntimeField(
                fieldName,
                xmlName,
                optionalText(node, "displayName"),
                nodeType,
                optionalEnum(node, "dataType", TemplateFieldValueType.class),
                optionalEnum(node, "sourceType", TemplateFieldSourceType.class),
                optionalEnum(node, "occurrenceRule", TemplateFieldOccurrenceRule.class),
                emptyHandling,
                node.path("requiredWhenParentExists").asBoolean(false),
                optionalBoolean(node, "triggerActivation"),
                optionalText(node, "defaultValue"),
                optionalText(node, "staticValue"),
                optionalText(node, "xmlPath"),
                optionalText(node, "namespace"),
                displayOrder,
                optionalText(node, "description"),
                loadChildren(node.get("children")));
    }

    private List<RuntimeField> loadChildren(JsonNode childrenNode) {
        if (childrenNode == null || childrenNode.isNull()) {
            return List.of();
        }
        if (!childrenNode.isArray()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                    "Compiled schema children must be an array");
        }
        return loadFields(childrenNode);
    }

    private static String requiredText(JsonNode node, String propertyName) {
        JsonNode valueNode = node.get(propertyName);
        if (valueNode == null || valueNode.isNull() || !valueNode.isTextual() || valueNode.asText().isBlank()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                    "Missing required property: " + propertyName);
        }
        return valueNode.asText();
    }

    private static String optionalText(JsonNode node, String propertyName) {
        JsonNode valueNode = node.get(propertyName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        if (!valueNode.isTextual()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                    "Property must be a string: " + propertyName);
        }
        return valueNode.asText();
    }

    private static Boolean optionalBoolean(JsonNode node, String propertyName) {
        JsonNode valueNode = node.get(propertyName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        if (!valueNode.isBoolean()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                    "Property must be a boolean: " + propertyName);
        }
        return valueNode.asBoolean();
    }

    private static int requiredInt(JsonNode node, String propertyName) {
        JsonNode valueNode = node.get(propertyName);
        if (valueNode == null || valueNode.isNull() || !valueNode.isNumber()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                    "Missing required numeric property: " + propertyName);
        }
        return valueNode.asInt();
    }

    private static <E extends Enum<E>> E requiredEnum(JsonNode node, String propertyName, Class<E> enumType) {
        String value = requiredText(node, propertyName);
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException ex) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                    "Invalid " + propertyName + ": " + value);
        }
    }

    private static <E extends Enum<E>> E optionalEnum(JsonNode node, String propertyName, Class<E> enumType) {
        JsonNode valueNode = node.get(propertyName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        if (!valueNode.isTextual()) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                    "Property must be a string: " + propertyName);
        }
        try {
            return Enum.valueOf(enumType, valueNode.asText());
        } catch (IllegalArgumentException ex) {
            throw loaderException(
                    RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID,
                    "Invalid " + propertyName + ": " + valueNode.asText());
        }
    }

    private static List<RuntimeField> sortFields(List<RuntimeField> fields) {
        return fields.stream()
                .sorted(Comparator.comparingInt(RuntimeField::displayOrder)
                        .thenComparing(RuntimeField::fieldName))
                .toList();
    }

    private static RuntimeLoaderException loaderException(RuntimeLoaderErrorCode errorCode, String message) {
        return new RuntimeLoaderException(errorCode, message);
    }
}
