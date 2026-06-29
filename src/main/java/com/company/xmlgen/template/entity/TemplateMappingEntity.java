package com.company.xmlgen.template.entity;

import com.company.xmlgen.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Persistence model for the {@code template_mappings} table.
 *
 * @see docs/03-database-design/03-database-design.md §4.8
 * @see docs/02-domain-model/02-domain-model.md §10
 */
@Entity
@Table(
        name = "template_mappings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_template_mappings_template_field",
                columnNames = "template_field_id"),
        indexes = {
                @Index(name = "idx_template_mappings_template_id", columnList = "template_id"),
                @Index(name = "idx_template_mappings_master_data_field_id", columnList = "master_data_field_id")
        })
public class TemplateMappingEntity extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "template_field_id", nullable = false)
    private Long templateFieldId;

    @Column(name = "master_data_field_id")
    private Long masterDataFieldId;

    protected TemplateMappingEntity() {
    }

    public TemplateMappingEntity(Long templateId, Long templateFieldId, Long masterDataFieldId) {
        this.templateId = templateId;
        this.templateFieldId = templateFieldId;
        this.masterDataFieldId = masterDataFieldId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getTemplateFieldId() {
        return templateFieldId;
    }

    public void setTemplateFieldId(Long templateFieldId) {
        this.templateFieldId = templateFieldId;
    }

    public Long getMasterDataFieldId() {
        return masterDataFieldId;
    }

    public void setMasterDataFieldId(Long masterDataFieldId) {
        this.masterDataFieldId = masterDataFieldId;
    }
}
