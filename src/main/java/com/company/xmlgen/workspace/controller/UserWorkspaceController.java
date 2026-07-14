package com.company.xmlgen.workspace.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.workspace.dto.request.AssignUserWorkspacesRequest;
import com.company.xmlgen.workspace.dto.response.UserWorkspaceResponse;
import com.company.xmlgen.workspace.service.UserWorkspaceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for assigning users to workspaces.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/workspaces")
public class UserWorkspaceController {

    private final UserWorkspaceService userWorkspaceService;

    public UserWorkspaceController(UserWorkspaceService userWorkspaceService) {
        this.userWorkspaceService = userWorkspaceService;
    }

    @GetMapping
    public ApiResponse<List<UserWorkspaceResponse>> findByUser(@PathVariable Long userId) {
        return ApiResponse.ok(userWorkspaceService.findByUser(userId));
    }

    @PutMapping
    public ApiResponse<List<UserWorkspaceResponse>> assign(
            @PathVariable Long userId, @Valid @RequestBody AssignUserWorkspacesRequest request) {
        return ApiResponse.ok(userWorkspaceService.assign(userId, request));
    }
}
