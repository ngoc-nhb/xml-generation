package com.company.xmlgen.workspace.entity;

/**
 * Role of a user within a {@link WorkspaceEntity}.
 *
 * @see docs/02-domain-model/p5_workspace-ownership.md §4
 */
public enum WorkspaceRole {

    WORKSPACE_ADMIN,
    WORKSPACE_USER;

    public static WorkspaceRole fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return valueOf(value.trim().toUpperCase());
    }
}
