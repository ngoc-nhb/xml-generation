package com.company.xmlgen.workspace.mapper;

import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.UpdateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceListResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceMemberResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceResponse;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceMemberEntity;
import com.company.xmlgen.workspace.entity.WorkspaceRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Maps {@link WorkspaceEntity} to workspace API response DTOs.
 */
@Component
public class WorkspaceMapper {

    public WorkspaceListResponse toListResponse(
            WorkspaceEntity entity, WorkspaceRole myRole, Set<String> myPermissions) {
        List<String> permissions =
                myPermissions == null ? null : new ArrayList<>(myPermissions);
        return new WorkspaceListResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getStatus(),
                entity.getType(),
                myRole,
                permissions);
    }

    public WorkspaceMemberResponse toMemberResponse(WorkspaceMemberEntity member) {
        return new WorkspaceMemberResponse(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getRole(),
                new ArrayList<>(member.getPermissionCodes()));
    }

    public WorkspaceResponse toResponse(WorkspaceEntity entity) {
        return new WorkspaceResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getType(),
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
                entity.getStatus(),
                entity.getType());
    }

    public UpdateWorkspaceResponse toUpdateResponse(WorkspaceEntity entity) {
        return new UpdateWorkspaceResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getType());
    }
}
