package com.company.xmlgen.workspace.entity;

import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.common.persistence.BaseEntity;
import com.company.xmlgen.workspace.domain.WorkspacePermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Persistence model for the {@code workspace_members} table.
 *
 * <p>Links a {@link UserEntity} to a {@link WorkspaceEntity} with a {@link WorkspaceRole} and
 * an extensible set of capability codes in {@code permissions}.
 *
 * @see docs/02-domain-model/p5_workspace-ownership.md §4
 * @see docs/03-database-design/03-database-design.md §4.12
 */
@Entity
@Table(
        name = "workspace_members",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_workspace_members_workspace_user",
                        columnNames = {"workspace_id", "user_id"}),
        indexes = {
            @Index(name = "idx_workspace_members_workspace_id", columnList = "workspace_id"),
            @Index(name = "idx_workspace_members_user_id", columnList = "user_id")
        })
public class WorkspaceMemberEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private WorkspaceRole role;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", nullable = false, columnDefinition = "jsonb")
    private Set<String> permissions = new LinkedHashSet<>();

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected WorkspaceMemberEntity() {
    }

    public WorkspaceMemberEntity(
            WorkspaceEntity workspace, UserEntity user, WorkspaceRole role, Instant joinedAt) {
        this.workspace = workspace;
        this.user = user;
        this.role = role;
        this.joinedAt = joinedAt;
        this.permissions = new LinkedHashSet<>();
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceEntity workspace) {
        this.workspace = workspace;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public WorkspaceRole getRole() {
        return role;
    }

    public void setRole(WorkspaceRole role) {
        this.role = role;
    }

    public Set<String> getPermissionCodes() {
        if (permissions == null) {
            return Set.of();
        }
        return Collections.unmodifiableSet(permissions);
    }

    public void setPermissionCodes(Set<String> permissionCodes) {
        this.permissions = new LinkedHashSet<>(WorkspacePermission.normalizeCodes(permissionCodes));
    }

    public boolean hasPermission(WorkspacePermission permission) {
        return permissions != null && permissions.contains(permission.name());
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
