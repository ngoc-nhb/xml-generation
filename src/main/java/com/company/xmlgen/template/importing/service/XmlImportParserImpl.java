package com.company.xmlgen.template.importing.service;

import com.company.xmlgen.template.importing.domain.XmlImportNode;
import com.company.xmlgen.template.importing.domain.XmlImportNodeType;
import com.company.xmlgen.template.importing.exception.XmlImportErrorCode;
import com.company.xmlgen.template.importing.exception.XmlImportException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * DOM-based XML parser for template import.
 */
@Service
public class XmlImportParserImpl implements XmlImportParser {

    @Override
    public XmlImportNode parse(byte[] xmlBytes) {
        if (xmlBytes == null || xmlBytes.length == 0) {
            throw new XmlImportException(XmlImportErrorCode.XML_IMPORT_EMPTY, "XML file is empty.");
        }

        String xml = new String(xmlBytes, StandardCharsets.UTF_8).trim();
        if (xml.isEmpty()) {
            throw new XmlImportException(XmlImportErrorCode.XML_IMPORT_EMPTY, "XML file is empty.");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlBytes));
            document.getDocumentElement().normalize();

            Element documentElement = document.getDocumentElement();
            if (documentElement == null) {
                throw new XmlImportException(XmlImportErrorCode.XML_IMPORT_EMPTY, "XML file has no root element.");
            }

            validateSingleRootElement(document);
            return convertElement(documentElement, null);
        } catch (XmlImportException ex) {
            throw ex;
        } catch (SAXParseException ex) {
            throw new XmlImportException(
                    XmlImportErrorCode.XML_IMPORT_MALFORMED,
                    "XML is malformed: " + ex.getLocalizedMessage());
        } catch (Exception ex) {
            throw new XmlImportException(
                    XmlImportErrorCode.XML_IMPORT_MALFORMED,
                    "XML is malformed: " + ex.getMessage());
        }
    }

    private static void validateSingleRootElement(Document document) {
        Element root = document.getDocumentElement();
        Node sibling = root.getNextSibling();
        while (sibling != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                throw new XmlImportException(
                        XmlImportErrorCode.XML_IMPORT_MULTIPLE_ROOTS,
                        "XML must contain exactly one root element.");
            }
            sibling = sibling.getNextSibling();
        }
    }

    private XmlImportNode convertElement(Element element, XmlImportNode parent) {
        validateAttributes(element);
        rejectUnsupportedNodes(element);

        List<Element> childElements = directChildElements(element);
        String directText = directTextContent(element);
        boolean hasElementChildren = !childElements.isEmpty();
        boolean hasAttributes = element.hasAttributes();
        boolean hasText = !directText.isEmpty();

        if (hasElementChildren && hasText) {
            throw new XmlImportException(
                    XmlImportErrorCode.XML_IMPORT_UNSUPPORTED_CONSTRUCT,
                    "Mixed text and child elements are not supported at element "
                            + element.getTagName()
                            + ".");
        }

        if (!hasElementChildren && !hasAttributes) {
            return new XmlImportNode(
                    element.getTagName(),
                    XmlImportNodeType.ELEMENT,
                    directText,
                    parent,
                    List.of());
        }

        XmlImportNode group = new XmlImportNode(element.getTagName(), XmlImportNodeType.ELEMENT, null, parent, new ArrayList<>());
        int displayOrderSeed = 1;

        for (int index = 0; index < element.getAttributes().getLength(); index++) {
            Attr attribute = (Attr) element.getAttributes().item(index);
            XmlImportNode attributeNode = new XmlImportNode(
                    attribute.getName(),
                    XmlImportNodeType.ATTRIBUTE,
                    attribute.getValue(),
                    group,
                    List.of());
            group.addChild(attributeNode);
            displayOrderSeed++;
        }

        for (Element childElement : childElements) {
            group.addChild(convertElement(childElement, group));
            displayOrderSeed++;
        }

        if (!hasElementChildren && !hasAttributes && hasText) {
            return new XmlImportNode(
                    element.getTagName(),
                    XmlImportNodeType.ELEMENT,
                    directText,
                    parent,
                    List.of());
        }

        return group;
    }

    private static void validateAttributes(Element element) {
        Set<String> seen = new HashSet<>();
        for (int index = 0; index < element.getAttributes().getLength(); index++) {
            Attr attribute = (Attr) element.getAttributes().item(index);
            if (!seen.add(attribute.getName())) {
                throw new XmlImportException(
                        XmlImportErrorCode.XML_IMPORT_DUPLICATE_ATTRIBUTE,
                        "Duplicate attribute '"
                                + attribute.getName()
                                + "' on element "
                                + element.getTagName()
                                + ".");
            }
        }
    }

    private static void rejectUnsupportedNodes(Element element) {
        NodeList children = element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE, Node.TEXT_NODE, Node.COMMENT_NODE -> {
                    // supported
                }
                case Node.CDATA_SECTION_NODE -> throw new XmlImportException(
                        XmlImportErrorCode.XML_IMPORT_UNSUPPORTED_CONSTRUCT,
                        "CDATA sections are not supported.");
                case Node.PROCESSING_INSTRUCTION_NODE -> throw new XmlImportException(
                        XmlImportErrorCode.XML_IMPORT_UNSUPPORTED_CONSTRUCT,
                        "Processing instructions are not supported.");
                default -> throw new XmlImportException(
                        XmlImportErrorCode.XML_IMPORT_UNSUPPORTED_CONSTRUCT,
                        "Unsupported XML construct: " + child.getNodeName());
            }
        }
    }

    private static List<Element> directChildElements(Element element) {
        List<Element> children = new ArrayList<>();
        NodeList nodes = element.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                children.add((Element) node);
            }
        }
        return children;
    }

    private static String directTextContent(Element element) {
        StringBuilder builder = new StringBuilder();
        NodeList nodes = element.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                builder.append(node.getNodeValue());
            }
        }
        String text = builder.toString();
        return text.isBlank() ? "" : text;
    }
}
