package com.company.xmlgen.workspace.service;

import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceMemberPermissionsRequest;
import com.company.xmlgen.workspace.dto.response.WorkspaceMemberResponse;
import java.util.List;

/**
 * Workspace membership listing and permission updates for workspace settings.
 */
public interface WorkspaceMemberService {

    List<WorkspaceMemberResponse> findByWorkspace(Long workspaceId);

    WorkspaceMemberResponse updatePermissions(
            Long workspaceId, Long userId, UpdateWorkspaceMemberPermissionsRequest request);
}
