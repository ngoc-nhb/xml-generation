package com.company.xmlgen.workspace.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Full membership set for a user. Personal workspace memberships are retained separately
 * and must not appear in this list.
 */
public record AssignUserWorkspacesRequest(@NotNull @Valid List<WorkspaceMembershipAssignment> memberships) {}
