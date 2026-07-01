package com.company.xmlgen.template.importing.service;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import com.company.xmlgen.template.importing.domain.XmlImportNode;
import com.company.xmlgen.template.importing.domain.XmlImportNodeType;
import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftFieldResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Builds template draft fields from a parsed XML tree.
 */
@Service
public class TemplateDraftBuilderImpl implements TemplateDraftBuilder {

    @Override
    public List<TemplateImportDraftFieldResponse> build(XmlImportNode root) {
        List<TemplateImportDraftFieldResponse> fields = new ArrayList<>();
        Set<String> usedFieldNames = new HashSet<>();
        Set<String> conflictingXmlNames = findConflictingXmlNames(root);
        appendNode(root, null, fields, usedFieldNames, conflictingXmlNames);
        return fields;
    }

    private String appendNode(
            XmlImportNode node,
            String parentFieldName,
            List<TemplateImportDraftFieldResponse> fields,
            Set<String> usedFieldNames,
            Set<String> conflictingXmlNames) {
        String xmlName = node.getNodeName();
        String fieldName = uniqueFieldName(xmlName, parentFieldName, usedFieldNames, conflictingXmlNames);
        usedFieldNames.add(fieldName);

        boolean isAttribute = node.getNodeType() == XmlImportNodeType.ATTRIBUTE;
        boolean isLeaf = node.getChildren().isEmpty();

        TemplateFieldNodeType nodeType;
        if (isAttribute) {
            nodeType = TemplateFieldNodeType.ATTRIBUTE;
        } else if (isLeaf) {
            nodeType = TemplateFieldNodeType.ELEMENT;
        } else {
            nodeType = TemplateFieldNodeType.GROUP;
        }

        int displayOrder = fields.stream()
                        .filter(field -> java.util.Objects.equals(field.parentFieldName(), parentFieldName))
                        .mapToInt(TemplateImportDraftFieldResponse::displayOrder)
                        .max()
                        .orElse(0)
                + 1;

        ImportedValue importedValue = resolveImportedValue(node, isLeaf, isAttribute);

        fields.add(new TemplateImportDraftFieldResponse(
                fieldName,
                parentFieldName,
                xmlName,
                xmlName,
                nodeType,
                isLeaf || isAttribute ? TemplateFieldValueType.STRING : null,
                null,
                null,
                importedValue.emptyHandling(),
                false,
                null,
                importedValue.defaultValue(),
                null,
                null,
                null,
                displayOrder,
                null,
                true));

        for (XmlImportNode child : node.getChildren()) {
            appendNode(child, fieldName, fields, usedFieldNames, conflictingXmlNames);
        }

        return fieldName;
    }

    /**
     * Empty sample values imply {@link TemplateFieldEmptyHandling#EMPTY_TAG_IF_EMPTY}; non-empty
     * values use {@link TemplateFieldEmptyHandling#REQUIRED} (no special empty handling).
     */
    private static ImportedValue resolveImportedValue(XmlImportNode node, boolean isLeaf, boolean isAttribute) {
        if (!isLeaf && !isAttribute) {
            return new ImportedValue(null, TemplateFieldEmptyHandling.REQUIRED);
        }

        String value = node.getValue();
        if (value == null || value.isEmpty()) {
            return new ImportedValue("", TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY);
        }
        return new ImportedValue(value, TemplateFieldEmptyHandling.REQUIRED);
    }

    private record ImportedValue(String defaultValue, TemplateFieldEmptyHandling emptyHandling) {}

    /**
     * XML names that appear under more than one parent require a parent-prefixed internal field name.
     */
    private static Set<String> findConflictingXmlNames(XmlImportNode root) {
        Map<String, Set<String>> parentsByXmlName = new HashMap<>();
        collectXmlNameParents(root, null, parentsByXmlName);

        Set<String> conflicting = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : parentsByXmlName.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicting.add(entry.getKey());
            }
        }
        return conflicting;
    }

    private static void collectXmlNameParents(
            XmlImportNode node, String parentXmlName, Map<String, Set<String>> parentsByXmlName) {
        String sanitizedXmlName = sanitizeFieldName(node.getNodeName());
        String parentKey = parentXmlName == null ? "" : parentXmlName;
        parentsByXmlName.computeIfAbsent(sanitizedXmlName, ignored -> new HashSet<>()).add(parentKey);

        for (XmlImportNode child : node.getChildren()) {
            collectXmlNameParents(child, node.getNodeName(), parentsByXmlName);
        }
    }

    private static String uniqueFieldName(
            String xmlName,
            String parentFieldName,
            Set<String> usedFieldNames,
            Set<String> conflictingXmlNames) {
        String sanitizedXmlName = sanitizeFieldName(xmlName);
        String candidate = buildFieldNameCandidate(sanitizedXmlName, parentFieldName, usedFieldNames, conflictingXmlNames);

        if (!usedFieldNames.contains(candidate)) {
            return candidate;
        }

        int suffix = 2;
        while (usedFieldNames.contains(candidate + "_" + suffix)) {
            suffix++;
        }
        return candidate + "_" + suffix;
    }

    private static String buildFieldNameCandidate(
            String sanitizedXmlName,
            String parentFieldName,
            Set<String> usedFieldNames,
            Set<String> conflictingXmlNames) {
        if (parentFieldName == null) {
            return sanitizedXmlName;
        }
        if (conflictingXmlNames.contains(sanitizedXmlName) || usedFieldNames.contains(sanitizedXmlName)) {
            return parentFieldName + "_" + sanitizedXmlName;
        }
        return sanitizedXmlName;
    }

    private static String sanitizeFieldName(String xmlName) {
        String sanitized = xmlName.replaceAll("[^A-Za-z0-9_]", "_");
        if (sanitized.isBlank()) {
            return "Field";
        }
        if (!Character.isLetter(sanitized.charAt(0)) && sanitized.charAt(0) != '_') {
            sanitized = "F_" + sanitized;
        }
        return sanitized;
    }
}
