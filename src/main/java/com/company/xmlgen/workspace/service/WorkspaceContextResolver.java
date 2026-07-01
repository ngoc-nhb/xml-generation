package com.company.xmlgen.workspace.service;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.workspace.context.WorkspaceContext;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Component;

/**
 * Resolves and validates the active workspace for the current request.
 *
 * <p>No permission checks, membership validation, or business-resource lookups.
 */
@Component
public class WorkspaceContextResolver {

    private final WorkspaceRepository workspaceRepository;

    public WorkspaceContextResolver(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public WorkspaceContext resolve(Long workspaceId) {
        WorkspaceEntity workspace = workspaceRepository
                .findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new BusinessException(WorkspaceErrorCode.INVALID_WORKSPACE));

        if (workspace.getStatus() == WorkspaceStatus.INACTIVE) {
            throw new ConflictException(WorkspaceErrorCode.WORKSPACE_INACTIVE);
        }

        return new WorkspaceContext(workspace.getId(), workspace.getCode());
    }
}
