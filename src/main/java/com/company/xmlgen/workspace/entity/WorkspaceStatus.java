package com.company.xmlgen.workspace.entity;

/**
 * Lifecycle status of a {@link WorkspaceEntity}.
 *
 * @see docs/02-domain-model/p5_workspace-ownership.md §3
 */
public enum WorkspaceStatus {

    ACTIVE,
    INACTIVE;

    public static WorkspaceStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return valueOf(value.trim().toUpperCase());
    }
}
