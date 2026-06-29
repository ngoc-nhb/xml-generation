package com.company.xmlgen.template.entity;

import com.company.xmlgen.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Persistence model for the {@code template_fields} table.
 *
 * @see docs/03-database-design/03-database-design.md §4.3
 * @see docs/02-domain-model/02-domain-model.md §5
 */
@Entity
@Table(
        name = "template_fields",
        indexes = {
                @Index(name = "idx_template_fields_template_id", columnList = "template_id"),
                @Index(name = "idx_template_fields_parent_id", columnList = "parent_id"),
                @Index(name = "idx_template_fields_template_display_order", columnList = "template_id, display_order")
        })
public class TemplateFieldEntity extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "field_name", nullable = false, length = 255)
    private String fieldName;

    @Column(name = "xml_name", nullable = false, length = 255)
    private String xmlName;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 20)
    private TemplateFieldNodeType nodeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 50)
    private TemplateFieldValueType valueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20)
    private TemplateFieldSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "occurrence_rule", length = 30)
    private TemplateFieldOccurrenceRule occurrenceRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "empty_handling", nullable = false, length = 30)
    private TemplateFieldEmptyHandling emptyHandling;

    @Column(name = "required_when_parent_exists", nullable = false)
    private boolean requiredWhenParentExists;

    @Column(name = "trigger_activation")
    private Boolean triggerActivation;

    @Column(name = "default_value", columnDefinition = "text")
    private String defaultValue;

    @Column(name = "static_value", columnDefinition = "text")
    private String staticValue;

    @Column(name = "xml_path", length = 1000)
    private String xmlPath;

    @Column(name = "namespace", length = 255)
    private String namespace;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    protected TemplateFieldEntity() {
    }

    public TemplateFieldEntity(
            Long templateId,
            String fieldName,
            String xmlName,
            TemplateFieldNodeType nodeType,
            TemplateFieldEmptyHandling emptyHandling,
            int displayOrder) {
        this.templateId = templateId;
        this.fieldName = fieldName;
        this.xmlName = xmlName;
        this.nodeType = nodeType;
        this.emptyHandling = emptyHandling;
        this.displayOrder = displayOrder;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getXmlName() {
        return xmlName;
    }

    public void setXmlName(String xmlName) {
        this.xmlName = xmlName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public TemplateFieldNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(TemplateFieldNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public TemplateFieldValueType getValueType() {
        return valueType;
    }

    public void setValueType(TemplateFieldValueType valueType) {
        this.valueType = valueType;
    }

    public TemplateFieldSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(TemplateFieldSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public TemplateFieldOccurrenceRule getOccurrenceRule() {
        return occurrenceRule;
    }

    public void setOccurrenceRule(TemplateFieldOccurrenceRule occurrenceRule) {
        this.occurrenceRule = occurrenceRule;
    }

    public TemplateFieldEmptyHandling getEmptyHandling() {
        return emptyHandling;
    }

    public void setEmptyHandling(TemplateFieldEmptyHandling emptyHandling) {
        this.emptyHandling = emptyHandling;
    }

    public boolean isRequiredWhenParentExists() {
        return requiredWhenParentExists;
    }

    public void setRequiredWhenParentExists(boolean requiredWhenParentExists) {
        this.requiredWhenParentExists = requiredWhenParentExists;
    }

    public Boolean getTriggerActivation() {
        return triggerActivation;
    }

    public void setTriggerActivation(Boolean triggerActivation) {
        this.triggerActivation = triggerActivation;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getStaticValue() {
        return staticValue;
    }

    public void setStaticValue(String staticValue) {
        this.staticValue = staticValue;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
