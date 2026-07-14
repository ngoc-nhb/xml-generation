package com.company.xmlgen.workspace.dto.response;

import com.company.xmlgen.workspace.entity.WorkspaceRole;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import java.util.List;

/**
 * Workspace membership row returned by user↔workspace assignment APIs.
 */
public record UserWorkspaceResponse(
        Long workspaceId,
        String code,
        String name,
        WorkspaceType type,
        WorkspaceRole role,
        List<String> permissions) {}
