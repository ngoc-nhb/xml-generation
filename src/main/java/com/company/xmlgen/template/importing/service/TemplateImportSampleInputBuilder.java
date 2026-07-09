package com.company.xmlgen.template.importing.service;

import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.importing.domain.XmlImportNode;
import com.company.xmlgen.template.importing.domain.XmlImportNodeType;
import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftFieldResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Builds sample input JSON from a parsed XML tree and imported field metadata.
 *
 * <p>The output matches the {@code inputData} shape consumed by XML Generation.
 */
@Service
public class TemplateImportSampleInputBuilder {

    private final ObjectMapper objectMapper;

    public TemplateImportSampleInputBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode build(XmlImportNode root, List<TemplateImportDraftFieldResponse> fields) {
        ObjectNode result = objectMapper.createObjectNode();
        for (TemplateImportDraftFieldResponse rootField : rootFields(fields)) {
            if (isContainer(rootField, fields)) {
                for (TemplateImportDraftFieldResponse child : childrenOf(rootField.fieldName(), fields)) {
                    JsonNode value = buildFieldValue(child, root, fields);
                    if (value != null) {
                        result.set(child.fieldName(), value);
                    }
                }
                continue;
            }

            JsonNode value = buildFieldValue(rootField, root, fields);
            if (value != null) {
                result.set(rootField.fieldName(), value);
            }
        }
        return result;
    }

    private JsonNode buildFieldValue(
            TemplateImportDraftFieldResponse field, XmlImportNode xmlContext, List<TemplateImportDraftFieldResponse> fields) {
        if (field.nodeType() == TemplateFieldNodeType.ATTRIBUTE) {
            XmlImportNode attribute = findAttributeChild(xmlContext, field.xmlName());
            return attribute == null ? null : textValue(attribute.getValue());
        }

        if (field.nodeType() == TemplateFieldNodeType.ELEMENT) {
            XmlImportNode element = findFirstElementChild(xmlContext, field.xmlName());
            return element == null ? null : textValue(element.getValue());
        }

        if (field.nodeType() != TemplateFieldNodeType.GROUP) {
            return null;
        }

        List<XmlImportNode> xmlNodes = findElementChildren(xmlContext, field.xmlName());
        if (isRepeatable(field.occurrenceRule())) {
            ArrayNode array = objectMapper.createArrayNode();
            for (XmlImportNode xmlNode : xmlNodes) {
                array.add(buildGroupObject(field, xmlNode, fields));
            }
            return array;
        }

        if (xmlNodes.isEmpty()) {
            return objectMapper.createObjectNode();
        }
        return buildGroupObject(field, xmlNodes.getFirst(), fields);
    }

    private ObjectNode buildGroupObject(
            TemplateImportDraftFieldResponse groupField,
            XmlImportNode xmlNode,
            List<TemplateImportDraftFieldResponse> fields) {
        ObjectNode object = objectMapper.createObjectNode();
        for (TemplateImportDraftFieldResponse child : childrenOf(groupField.fieldName(), fields)) {
            JsonNode value = buildFieldValue(child, xmlNode, fields);
            if (value != null) {
                object.set(child.fieldName(), value);
            }
        }
        return object;
    }

    private static List<TemplateImportDraftFieldResponse> rootFields(List<TemplateImportDraftFieldResponse> fields) {
        return fields.stream()
                .filter(field -> field.parentFieldName() == null)
                .sorted(Comparator.comparingInt(TemplateImportDraftFieldResponse::displayOrder))
                .toList();
    }

    private static List<TemplateImportDraftFieldResponse> childrenOf(
            String parentFieldName, List<TemplateImportDraftFieldResponse> fields) {
        return fields.stream()
                .filter(field -> parentFieldName.equals(field.parentFieldName()))
                .sorted(Comparator.comparingInt(TemplateImportDraftFieldResponse::displayOrder))
                .toList();
    }

    private static boolean isContainer(TemplateImportDraftFieldResponse field, List<TemplateImportDraftFieldResponse> fields) {
        return field.nodeType() == TemplateFieldNodeType.GROUP
                || fields.stream().anyMatch(item -> field.fieldName().equals(item.parentFieldName()));
    }

    private static boolean isRepeatable(TemplateFieldOccurrenceRule occurrenceRule) {
        return occurrenceRule == TemplateFieldOccurrenceRule.ONE_OR_MORE
                || occurrenceRule == TemplateFieldOccurrenceRule.ZERO_OR_MORE;
    }

    private static XmlImportNode findAttributeChild(XmlImportNode parent, String xmlName) {
        for (XmlImportNode child : parent.getChildren()) {
            if (child.getNodeType() == XmlImportNodeType.ATTRIBUTE && xmlName.equals(child.getNodeName())) {
                return child;
            }
        }
        return null;
    }

    private static XmlImportNode findFirstElementChild(XmlImportNode parent, String xmlName) {
        for (XmlImportNode child : parent.getChildren()) {
            if (child.getNodeType() == XmlImportNodeType.ELEMENT && xmlName.equals(child.getNodeName())) {
                return child;
            }
        }
        return null;
    }

    private static List<XmlImportNode> findElementChildren(XmlImportNode parent, String xmlName) {
        List<XmlImportNode> matches = new ArrayList<>();
        for (XmlImportNode child : parent.getChildren()) {
            if (child.getNodeType() == XmlImportNodeType.ELEMENT && xmlName.equals(child.getNodeName())) {
                matches.add(child);
            }
        }
        return matches;
    }

    private static JsonNode textValue(String value) {
        return new TextNode(value == null ? "" : value);
    }
}
