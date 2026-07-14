package com.company.xmlgen.workspace.entity;

import com.company.xmlgen.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Persistence model for the {@code workspaces} table.
 *
 * <p>Workspace is an ownership boundary, not a transactional aggregate root for Template or
 * Master Data.
 *
 * @see docs/02-domain-model/p5_workspace-ownership.md §3
 * @see docs/03-database-design/03-database-design.md §4.11
 */
@Entity
@Table(
        name = "workspaces",
        uniqueConstraints = @UniqueConstraint(name = "uk_workspaces_code", columnNames = "code"),
        indexes = {
            @Index(name = "idx_workspaces_status", columnList = "status"),
            @Index(name = "idx_workspaces_created_by", columnList = "created_by"),
            @Index(name = "idx_workspaces_type", columnList = "workspace_type")
        })
public class WorkspaceEntity extends BaseEntity {

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkspaceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "workspace_type", nullable = false, length = 20)
    private WorkspaceType type;

    @Column(name = "created_by", nullable = false)
    private Long createdById;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected WorkspaceEntity() {
    }

    public WorkspaceEntity(
            String code, String name, WorkspaceStatus status, WorkspaceType type, Long createdById) {
        this.code = code;
        this.name = name;
        this.status = status;
        this.type = type;
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

    public WorkspaceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
    }

    public WorkspaceType getType() {
        return type;
    }

    public void setType(WorkspaceType type) {
        this.type = type;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
