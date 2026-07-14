package com.company.xmlgen.workspace.service;

import com.company.xmlgen.workspace.dto.request.AssignUserWorkspacesRequest;
import com.company.xmlgen.workspace.dto.response.UserWorkspaceResponse;
import java.util.List;

/**
 * Admin-managed assignment of users to workspaces.
 */
public interface UserWorkspaceService {

    List<UserWorkspaceResponse> findByUser(Long userId);

    List<UserWorkspaceResponse> assign(Long userId, AssignUserWorkspacesRequest request);
}
