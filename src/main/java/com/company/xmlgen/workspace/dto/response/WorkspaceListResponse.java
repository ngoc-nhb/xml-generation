package com.company.xmlgen.workspace.dto.response;

import com.company.xmlgen.workspace.entity.WorkspaceRole;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import java.util.List;

/**
 * List item for {@code GET /api/v1/workspaces}.
 *
 * <p>{@code myRole} / {@code myPermissions} describe the caller's membership in the workspace.
 * Both are {@code null} for system admins (who access every workspace without membership).
 */
public record WorkspaceListResponse(
        Long id,
        String code,
        String name,
        WorkspaceStatus status,
        WorkspaceType type,
        WorkspaceRole myRole,
        List<String> myPermissions) {}
