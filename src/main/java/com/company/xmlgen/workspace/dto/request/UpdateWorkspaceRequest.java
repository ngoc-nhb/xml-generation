package com.company.xmlgen.workspace.dto.request;

import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code PUT /api/v1/workspaces/{id}}.
 *
 * <p>Workspace {@code code} is immutable after creation and is not accepted in this payload.
 */
public record UpdateWorkspaceRequest(
        @NotBlank String name, String description, @NotNull WorkspaceStatus status) {}
