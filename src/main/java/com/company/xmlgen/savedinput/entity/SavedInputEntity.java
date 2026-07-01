package com.company.xmlgen.savedinput.entity;

import com.company.xmlgen.common.persistence.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Persistence model for the {@code saved_inputs} table.
 *
 * @see docs/03-database-design/03-database-design.md §4.9
 */
@Entity
@Table(
        name = "saved_inputs",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_saved_inputs_workspace_user_template",
                        columnNames = {"workspace_id", "user_id", "template_id"}),
        indexes = {
            @Index(name = "idx_saved_inputs_workspace_id", columnList = "workspace_id"),
            @Index(name = "idx_saved_inputs_user_id", columnList = "user_id"),
            @Index(name = "idx_saved_inputs_template_id", columnList = "template_id")
        })
public class SavedInputEntity extends BaseEntity {

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_data_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode inputDataJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_master_data_json", columnDefinition = "jsonb")
    private JsonNode selectedMasterDataJson;

    protected SavedInputEntity() {}

    public SavedInputEntity(
            Long workspaceId,
            Long userId,
            Long templateId,
            JsonNode inputDataJson,
            JsonNode selectedMasterDataJson) {
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.templateId = templateId;
        this.inputDataJson = inputDataJson;
        this.selectedMasterDataJson = selectedMasterDataJson;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public JsonNode getInputDataJson() {
        return inputDataJson;
    }

    public void setInputDataJson(JsonNode inputDataJson) {
        this.inputDataJson = inputDataJson;
    }

    public JsonNode getSelectedMasterDataJson() {
        return selectedMasterDataJson;
    }

    public void setSelectedMasterDataJson(JsonNode selectedMasterDataJson) {
        this.selectedMasterDataJson = selectedMasterDataJson;
    }
}
