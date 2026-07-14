package com.company.xmlgen.workspace.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Replace the capability set for one workspace membership.
 */
public record UpdateWorkspaceMemberPermissionsRequest(@NotNull List<String> permissions) {}
