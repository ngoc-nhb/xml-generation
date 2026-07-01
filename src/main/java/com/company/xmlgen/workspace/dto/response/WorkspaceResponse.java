package com.company.xmlgen.workspace.dto.response;

import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import java.time.Instant;

/**
 * Detail payload for {@code GET /api/v1/workspaces/{id}}.
 */
public record WorkspaceResponse(
        Long id,
        String code,
        String name,
        String description,
        WorkspaceStatus status,
        Long createdById,
        Instant createdAt,
        Instant updatedAt) {}
