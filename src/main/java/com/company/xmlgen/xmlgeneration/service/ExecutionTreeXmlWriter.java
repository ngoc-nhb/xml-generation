package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionNode;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Package-private StAX serializer used by {@link XMLGenerationServiceImpl}.
 *
 * <p>This is an internal implementation detail. It is not a pipeline component and must not
 * appear in architecture diagrams. Only {@link XMLGenerationService} is the public XML generation
 * boundary.
 */
final class ExecutionTreeXmlWriter {

    String write(RuntimeExecutionTree executionTree) throws XMLStreamException {
        StringWriter output = new StringWriter();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
        writer.writeStartDocument("UTF-8", "1.0");
        writeNode(executionTree.roots().getFirst(), writer);
        writer.writeEndDocument();
        writer.close();
        return output.toString();
    }

    private void writeNode(RuntimeExecutionNode node, XMLStreamWriter writer) throws XMLStreamException {
        TemplateFieldNodeType nodeType = node.field().nodeType();
        if (nodeType == TemplateFieldNodeType.ATTRIBUTE) {
            return;
        }
        if (nodeType == TemplateFieldNodeType.GROUP) {
            writeGroup(node, writer);
            return;
        }
        writeElement(node, writer);
    }

    private void writeGroup(RuntimeExecutionNode node, XMLStreamWriter writer) throws XMLStreamException {
        if (shouldOmitGroup(node)) {
            return;
        }

        List<RuntimeExecutionNode> attributes = attributeChildren(node);
        List<RuntimeExecutionNode> bodyChildren = bodyChildren(node);

        writer.writeStartElement(node.field().xmlName());
        writeAttributes(attributes, writer);
        for (RuntimeExecutionNode child : bodyChildren) {
            writeNode(child, writer);
        }
        writer.writeEndElement();
    }

    private void writeElement(RuntimeExecutionNode node, XMLStreamWriter writer) throws XMLStreamException {
        if (shouldOmitElement(node)) {
            return;
        }

        List<RuntimeExecutionNode> attributes = attributeChildren(node);
        List<RuntimeExecutionNode> bodyChildren = bodyChildren(node);

        writer.writeStartElement(node.field().xmlName());
        writeAttributes(attributes, writer);
        writeElementText(node, writer);
        for (RuntimeExecutionNode child : bodyChildren) {
            writeNode(child, writer);
        }
        writer.writeEndElement();
    }

    private void writeAttributes(List<RuntimeExecutionNode> attributes, XMLStreamWriter writer)
            throws XMLStreamException {
        for (RuntimeExecutionNode attribute : attributes) {
            if (shouldOmitAttribute(attribute)) {
                continue;
            }
            writer.writeAttribute(attribute.field().xmlName(), serializeValue(attribute));
        }
    }

    private void writeElementText(RuntimeExecutionNode node, XMLStreamWriter writer) throws XMLStreamException {
        JsonNode value = node.value();
        if (!isEmptyValue(value)) {
            writer.writeCharacters(value.asText());
            return;
        }

        if (node.field().emptyHandling() == TemplateFieldEmptyHandling.ZERO_IF_EMPTY) {
            writer.writeCharacters("0");
        }
    }

    private static boolean shouldOmitGroup(RuntimeExecutionNode node) {
        if (node.field().emptyHandling() == TemplateFieldEmptyHandling.OMIT_IF_EMPTY) {
            return !hasVisibleContent(node);
        }
        if (node.field().emptyHandling() == TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY) {
            return false;
        }
        return false;
    }

    private static boolean shouldOmitElement(RuntimeExecutionNode node) {
        if (node.field().emptyHandling() == TemplateFieldEmptyHandling.OMIT_IF_EMPTY) {
            return isEmptyValue(node.value()) && !hasVisibleBodyChildren(node);
        }
        return false;
    }

    private static boolean shouldOmitAttribute(RuntimeExecutionNode node) {
        return node.field().emptyHandling() == TemplateFieldEmptyHandling.OMIT_IF_EMPTY && isEmptyValue(node.value());
    }

    private static boolean hasVisibleContent(RuntimeExecutionNode groupNode) {
        for (RuntimeExecutionNode child : groupNode.children()) {
            if (child.field().nodeType() == TemplateFieldNodeType.ATTRIBUTE) {
                if (!shouldOmitAttribute(child)) {
                    return true;
                }
                continue;
            }
            if (child.field().nodeType() == TemplateFieldNodeType.GROUP) {
                if (!shouldOmitGroup(child)) {
                    return true;
                }
                continue;
            }
            if (!shouldOmitElement(child)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasVisibleBodyChildren(RuntimeExecutionNode elementNode) {
        for (RuntimeExecutionNode child : bodyChildren(elementNode)) {
            if (child.field().nodeType() == TemplateFieldNodeType.GROUP) {
                if (!shouldOmitGroup(child)) {
                    return true;
                }
                continue;
            }
            if (!shouldOmitElement(child)) {
                return true;
            }
        }
        return false;
    }

    private static List<RuntimeExecutionNode> attributeChildren(RuntimeExecutionNode node) {
        List<RuntimeExecutionNode> attributes = new ArrayList<>();
        for (RuntimeExecutionNode child : node.children()) {
            if (child.field().nodeType() == TemplateFieldNodeType.ATTRIBUTE) {
                attributes.add(child);
            }
        }
        return sorted(attributes);
    }

    private static List<RuntimeExecutionNode> bodyChildren(RuntimeExecutionNode node) {
        List<RuntimeExecutionNode> bodyChildren = new ArrayList<>();
        for (RuntimeExecutionNode child : node.children()) {
            if (child.field().nodeType() != TemplateFieldNodeType.ATTRIBUTE) {
                bodyChildren.add(child);
            }
        }
        return sorted(bodyChildren);
    }

    private static List<RuntimeExecutionNode> sorted(List<RuntimeExecutionNode> nodes) {
        return nodes.stream()
                .sorted(Comparator.comparingInt((RuntimeExecutionNode executionNode) ->
                                executionNode.field().displayOrder())
                        .thenComparing(executionNode -> executionNode.field().fieldName()))
                .toList();
    }

    private static String serializeValue(RuntimeExecutionNode node) {
        JsonNode value = node.value();
        if (isEmptyValue(value)) {
            if (node.field().emptyHandling() == TemplateFieldEmptyHandling.ZERO_IF_EMPTY) {
                return "0";
            }
            return "";
        }
        return value.asText();
    }

    private static boolean isEmptyValue(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return true;
        }
        return value.isTextual() && value.asText().isBlank();
    }
}
