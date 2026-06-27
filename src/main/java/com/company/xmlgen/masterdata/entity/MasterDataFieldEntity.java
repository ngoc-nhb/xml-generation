package com.company.xmlgen.masterdata.entity;

import com.company.xmlgen.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Persistence model for the {@code master_data_fields} table.
 *
 * @see docs/03-database-design/03-database-design.md §4.6
 */
@Entity
@Table(
        name = "master_data_fields",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_master_data_fields_type_field_name",
                columnNames = {"master_data_type_id", "field_name"}),
        indexes = @Index(name = "idx_master_data_fields_type_id", columnList = "master_data_type_id"))
public class MasterDataFieldEntity extends BaseEntity {

    @Column(name = "master_data_type_id", nullable = false)
    private Long masterDataTypeId;

    @Column(name = "field_name", nullable = false, length = 255)
    private String fieldName;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 50)
    private MasterDataFieldDataType dataType;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "default_value", columnDefinition = "text")
    private String defaultValue;

    @Column(name = "\"unique\"", nullable = false)
    private boolean unique;

    @Column(name = "searchable", nullable = false)
    private boolean searchable;

    @Column(name = "master_data_reference_type_id")
    private Long masterDataReferenceTypeId;

    protected MasterDataFieldEntity() {
    }

    public MasterDataFieldEntity(
            Long masterDataTypeId,
            String fieldName,
            String name,
            MasterDataFieldDataType dataType,
            boolean required,
            int displayOrder) {
        this.masterDataTypeId = masterDataTypeId;
        this.fieldName = fieldName;
        this.name = name;
        this.dataType = dataType;
        this.required = required;
        this.displayOrder = displayOrder;
    }

    public Long getMasterDataTypeId() {
        return masterDataTypeId;
    }

    public void setMasterDataTypeId(Long masterDataTypeId) {
        this.masterDataTypeId = masterDataTypeId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MasterDataFieldDataType getDataType() {
        return dataType;
    }

    public void setDataType(MasterDataFieldDataType dataType) {
        this.dataType = dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public Long getMasterDataReferenceTypeId() {
        return masterDataReferenceTypeId;
    }

    public void setMasterDataReferenceTypeId(Long masterDataReferenceTypeId) {
        this.masterDataReferenceTypeId = masterDataReferenceTypeId;
    }
}
