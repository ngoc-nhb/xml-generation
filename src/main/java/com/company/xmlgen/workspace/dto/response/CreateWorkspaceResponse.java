package com.company.xmlgen.workspace.dto.response;

import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.entity.WorkspaceType;

/**
 * Response {@code data} payload for workspace create operations.
 */
public record CreateWorkspaceResponse(
        Long id,
        String code,
        String name,
        String description,
        WorkspaceStatus status,
        WorkspaceType type) {}
