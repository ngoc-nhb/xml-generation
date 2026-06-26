package com.company.xmlgen.template.entity;

import com.company.xmlgen.common.persistence.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Persistence model for the {@code templates} table.
 *
 * @see docs/03-database-design/03-database-design.md §4.2
 * @see docs/02-domain-model/02-domain-model.md §4
 */
@Entity
@Table(
        name = "templates",
        uniqueConstraints = @UniqueConstraint(name = "uk_templates_code", columnNames = "code"),
        indexes = @Index(name = "idx_templates_status", columnList = "status"))
public class TemplateEntity extends BaseEntity {

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TemplateStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "compiled_schema_json", columnDefinition = "jsonb")
    private JsonNode compiledSchemaJson;

    @Column(name = "created_by", nullable = false)
    private Long createdById;

    protected TemplateEntity() {
    }

    public TemplateEntity(String code, String name, TemplateStatus status, Long createdById) {
        this.code = code;
        this.name = name;
        this.status = status;
        this.createdById = createdById;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public void setStatus(TemplateStatus status) {
        this.status = status;
    }

    public JsonNode getCompiledSchemaJson() {
        return compiledSchemaJson;
    }

    public void setCompiledSchemaJson(JsonNode compiledSchemaJson) {
        this.compiledSchemaJson = compiledSchemaJson;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
}
