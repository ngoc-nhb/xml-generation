package com.company.xmlgen.workspace.service;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.ValidationException;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.repository.WorkspaceOwnedResourceRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Component;

/**
 * Validates workspace business rules beyond Jakarta Bean Validation.
 */
@Component
public class WorkspaceValidator {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceOwnedResourceRepository ownedResourceRepository;

    public WorkspaceValidator(
            WorkspaceRepository workspaceRepository,
            WorkspaceOwnedResourceRepository ownedResourceRepository) {
        this.workspaceRepository = workspaceRepository;
        this.ownedResourceRepository = ownedResourceRepository;
    }

    public void validateCreate(CreateWorkspaceRequest request) {
        if (workspaceRepository.existsByCode(request.code().trim())) {
            throw new ConflictException(WorkspaceErrorCode.WORKSPACE_CODE_ALREADY_EXISTS);
        }
    }

    public void validateCodeImmutable(String currentCode, String requestedCode) {
        if (requestedCode != null && !requestedCode.equals(currentCode)) {
            throw new ValidationException(
                    WorkspaceErrorCode.WORKSPACE_CODE_IMMUTABLE, "code", "Workspace code cannot be changed.");
        }
    }

    public void validateDeletable(Long workspaceId) {
        if (ownedResourceRepository.hasOwnedResources(workspaceId)) {
            throw new BusinessException(WorkspaceErrorCode.WORKSPACE_IN_USE);
        }
    }
}
