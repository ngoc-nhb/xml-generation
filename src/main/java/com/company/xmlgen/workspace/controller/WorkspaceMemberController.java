package com.company.xmlgen.workspace.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceMemberPermissionsRequest;
import com.company.xmlgen.workspace.dto.response.WorkspaceMemberResponse;
import com.company.xmlgen.workspace.service.WorkspaceMemberService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workspace membership / permissions endpoints for Workspace Settings.
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/members")
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;

    public WorkspaceMemberController(WorkspaceMemberService workspaceMemberService) {
        this.workspaceMemberService = workspaceMemberService;
    }

    @GetMapping
    public ApiResponse<List<WorkspaceMemberResponse>> findByWorkspace(@PathVariable Long workspaceId) {
        return ApiResponse.ok(workspaceMemberService.findByWorkspace(workspaceId));
    }

    @PutMapping("/{userId}/permissions")
    public ApiResponse<WorkspaceMemberResponse> updatePermissions(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateWorkspaceMemberPermissionsRequest request) {
        return ApiResponse.ok(workspaceMemberService.updatePermissions(workspaceId, userId, request));
    }
}
