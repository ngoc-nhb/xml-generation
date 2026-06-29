package com.company.xmlgen.template.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileContext;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Deterministically serializes runtime Template schema into compiled schema JSON.
 */
@Service
public class TemplateSchemaCompilerImpl implements TemplateSchemaCompiler {

    private final ObjectMapper objectMapper;

    public TemplateSchemaCompilerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode compile(RuntimeTemplate runtimeTemplate, TemplateCompileContext compileContext) {
        RuntimeTemplate safeTemplate = runtimeTemplate == null ? new RuntimeTemplate(List.of()) : runtimeTemplate;
        TemplateCompileContext safeContext = compileContext == null ? TemplateCompileContext.empty() : compileContext;

        Map<String, TemplateCompileMapping> mappingByFieldName = safeContext.mappings().stream()
                .collect(Collectors.toMap(
                        TemplateCompileMapping::fieldName,
                        Function.identity(),
                        (first, ignored) -> first));

        ObjectNode root = objectMapper.createObjectNode();
        root.set("roots", compileFields(safeTemplate.roots(), mappingByFieldName));
        root.set("mappings", compileMappings(safeContext.mappings()));
        return root;
    }

    private ArrayNode compileFields(
            List<RuntimeField> fields, Map<String, TemplateCompileMapping> mappingByFieldName) {
        ArrayNode nodes = objectMapper.createArrayNode();
        sortedFields(fields).forEach(field -> nodes.add(compileField(field, mappingByFieldName)));
        return nodes;
    }

    private ObjectNode compileField(
            RuntimeField field, Map<String, TemplateCompileMapping> mappingByFieldName) {
        ObjectNode node = objectMapper.createObjectNode();
        putIfNotNull(node, "fieldName", field.fieldName());
        putIfNotNull(node, "name", field.xmlName());
        putIfNotNull(node, "displayName", field.displayName());
        putIfNotNull(node, "fieldType", field.nodeType());
        putIfNotNull(node, "dataType", field.valueType());
        putIfNotNull(node, "sourceType", field.sourceType());
        putIfNotNull(node, "occurrenceRule", field.occurrenceRule());
        putIfNotNull(node, "emptyHandling", field.emptyHandling());
        node.put("requiredWhenParentExists", field.requiredWhenParentExists());
        putIfNotNull(node, "triggerActivation", field.triggerActivation());
        putIfNotNull(node, "defaultValue", field.defaultValue());
        putIfNotNull(node, "staticValue", field.staticValue());
        putIfNotNull(node, "xmlPath", field.xmlPath());
        putIfNotNull(node, "namespace", field.namespace());
        node.put("displayOrder", field.displayOrder());
        putIfNotNull(node, "description", field.description());

        TemplateCompileMapping mapping = mappingByFieldName.get(field.fieldName());
        if (mapping != null) {
            putIfNotNull(node, "masterDataType", mapping.masterDataTypeCode());
            putIfNotNull(node, "masterDataField", mapping.masterDataFieldName());
        }

        node.set("children", compileFields(field.children(), mappingByFieldName));
        return node;
    }

    private ArrayNode compileMappings(List<TemplateCompileMapping> mappings) {
        ArrayNode nodes = objectMapper.createArrayNode();
        mappings.stream()
                .sorted(Comparator.comparing(TemplateCompileMapping::fieldName)
                        .thenComparing(TemplateCompileMapping::masterDataTypeCode)
                        .thenComparing(TemplateCompileMapping::masterDataFieldName))
                .forEach(mapping -> {
                    ObjectNode node = objectMapper.createObjectNode();
                    node.put("fieldName", mapping.fieldName());
                    node.put("masterDataType", mapping.masterDataTypeCode());
                    node.put("masterDataField", mapping.masterDataFieldName());
                    nodes.add(node);
                });
        return nodes;
    }

    private static List<RuntimeField> sortedFields(List<RuntimeField> fields) {
        return fields.stream()
                .sorted(Comparator.comparingInt(RuntimeField::displayOrder)
                        .thenComparing(RuntimeField::fieldName))
                .toList();
    }

    private static void putIfNotNull(ObjectNode node, String propertyName, Enum<?> value) {
        if (value != null) {
            node.put(propertyName, value.name());
        }
    }

    private static void putIfNotNull(ObjectNode node, String propertyName, String value) {
        if (value != null) {
            node.put(propertyName, value);
        }
    }

    private static void putIfNotNull(ObjectNode node, String propertyName, Boolean value) {
        if (value != null) {
            node.put(propertyName, value);
        }
    }
}
