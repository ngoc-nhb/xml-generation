package com.company.xmlgen.workspace.entity;

/**
 * Distinguishes organization (global) workspaces from user-owned personal workspaces.
 */
public enum WorkspaceType {
    GLOBAL,
    PERSONAL;

    public static WorkspaceType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return valueOf(value.trim().toUpperCase());
    }
}
