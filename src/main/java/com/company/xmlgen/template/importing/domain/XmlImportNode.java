package com.company.xmlgen.template.importing.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic tree node produced from an XML sample. Does not carry Template metadata.
 */
public final class XmlImportNode {

    private final String nodeName;
    private final XmlImportNodeType nodeType;
    private final String value;
    private final XmlImportNode parent;
    private final List<XmlImportNode> children;

    public XmlImportNode(
            String nodeName,
            XmlImportNodeType nodeType,
            String value,
            XmlImportNode parent,
            List<XmlImportNode> children) {
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.value = value;
        this.parent = parent;
        this.children = new ArrayList<>(children);
    }

    public String getNodeName() {
        return nodeName;
    }

    public XmlImportNodeType getNodeType() {
        return nodeType;
    }

    public String getValue() {
        return value;
    }

    public XmlImportNode getParent() {
        return parent;
    }

    public List<XmlImportNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(XmlImportNode child) {
        children.add(child);
    }
}
