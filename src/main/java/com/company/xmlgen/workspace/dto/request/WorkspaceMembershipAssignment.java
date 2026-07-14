package com.company.xmlgen.workspace.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * One global-workspace membership assignment with capability codes.
 */
public record WorkspaceMembershipAssignment(
        @NotNull Long workspaceId, @NotNull List<String> permissions) {}
