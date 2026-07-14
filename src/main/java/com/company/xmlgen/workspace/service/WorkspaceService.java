package com.company.xmlgen.workspace.service;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.workspace.dto.request.CreatePersonalWorkspaceRequest;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.UpdateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceListResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceResponse;

/**
 * Workspace lifecycle operations.
 *
 * @see docs/06-api-design/p9_workspace-api-strategy.md
 */
public interface WorkspaceService {

    PageResult<WorkspaceListResponse> findAll(int page, int pageSize);

    WorkspaceResponse findById(Long id);

    CreateWorkspaceResponse create(CreateWorkspaceRequest request);

    CreateWorkspaceResponse createPersonal(CreatePersonalWorkspaceRequest request);

    UpdateWorkspaceResponse update(Long id, UpdateWorkspaceRequest request);

    void delete(Long id);
}
