package com.company.xmlgen.workspace.dto.response;

import com.company.xmlgen.workspace.entity.WorkspaceStatus;

/**
 * List item for {@code GET /api/v1/workspaces}.
 */
public record WorkspaceListResponse(Long id, String code, String name, WorkspaceStatus status) {}
