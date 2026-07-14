package com.company.xmlgen.workspace.dto.request;

/**
 * Request body for {@code POST /api/v1/workspaces/personal}.
 *
 * <p>{@code name} defaults to {@code "{username} Workspace"} when omitted or blank.
 */
public record CreatePersonalWorkspaceRequest(String name) {}
