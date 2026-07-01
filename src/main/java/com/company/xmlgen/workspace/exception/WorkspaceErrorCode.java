package com.company.xmlgen.workspace.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Workspace module error codes.
 *
 * @see docs/06-api-design/p9_workspace-api-strategy.md §6
 */
public enum WorkspaceErrorCode implements ErrorCode {
    WORKSPACE_NOT_FOUND,
    WORKSPACE_CODE_ALREADY_EXISTS,
    WORKSPACE_IN_USE,
    WORKSPACE_CODE_IMMUTABLE,
    WORKSPACE_REQUIRED,
    WORKSPACE_INACTIVE,
    INVALID_WORKSPACE;

    @Override
    public String code() {
        return name();
    }
}
