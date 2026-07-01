package com.company.xmlgen.workspace.context;

/**
 * Immutable application-layer workspace boundary for the current request.
 *
 * <p>Contains workspace identity only. No user or permission information.
 *
 * @see docs/02-domain-model/p5_workspace-ownership.md §8
 */
public record WorkspaceContext(Long workspaceId, String workspaceCode) {}
