package com.company.xmlgen.workspace.dto.response;

import com.company.xmlgen.workspace.entity.WorkspaceStatus;

/**
 * Response {@code data} payload for {@code POST /api/v1/workspaces}.
 */
public record CreateWorkspaceResponse(
        Long id, String code, String name, String description, WorkspaceStatus status) {}
