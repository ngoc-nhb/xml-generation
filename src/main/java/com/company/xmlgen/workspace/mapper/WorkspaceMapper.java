package com.company.xmlgen.workspace.mapper;

import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.UpdateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceListResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceResponse;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import org.springframework.stereotype.Component;

/**
 * Maps {@link WorkspaceEntity} to workspace API response DTOs.
 */
@Component
public class WorkspaceMapper {

    public WorkspaceListResponse toListResponse(WorkspaceEntity entity) {
        return new WorkspaceListResponse(
                entity.getId(), entity.getCode(), entity.getName(), entity.getStatus());
    }

    public WorkspaceResponse toResponse(WorkspaceEntity entity) {
        return new WorkspaceResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedById(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public CreateWorkspaceResponse toCreateResponse(WorkspaceEntity entity) {
        return new CreateWorkspaceResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus());
    }

    public UpdateWorkspaceResponse toUpdateResponse(WorkspaceEntity entity) {
        return new UpdateWorkspaceResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus());
    }
}
