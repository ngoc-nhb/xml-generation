package com.company.xmlgen.workspace.dto.response;

import com.company.xmlgen.workspace.entity.WorkspaceRole;
import java.util.List;

/**
 * Membership row for workspace settings / admin assignment views.
 */
public record WorkspaceMemberResponse(
        Long userId,
        String username,
        WorkspaceRole role,
        List<String> permissions) {}
