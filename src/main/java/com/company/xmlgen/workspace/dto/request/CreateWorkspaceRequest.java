package com.company.xmlgen.workspace.dto.request;

import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/v1/workspaces}.
 */
public record CreateWorkspaceRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull WorkspaceStatus status) {}
