package com.company.xmlgen.workspace.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.ForbiddenException;
import com.company.xmlgen.workspace.context.WorkspaceContext;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves and validates the active workspace for the current request.
 *
 * <p>Non-admin callers must be members of the requested workspace; system admins
 * may enter any workspace.
 */
@Component
public class WorkspaceContextResolver {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceContextResolver(
            WorkspaceRepository workspaceRepository, WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public WorkspaceContext resolve(Long workspaceId) {
        WorkspaceEntity workspace = workspaceRepository
                .findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new BusinessException(WorkspaceErrorCode.INVALID_WORKSPACE));

        if (workspace.getStatus() == WorkspaceStatus.INACTIVE) {
            throw new ConflictException(WorkspaceErrorCode.WORKSPACE_INACTIVE);
        }

        requireMembership(workspace.getId());

        return new WorkspaceContext(workspace.getId(), workspace.getCode());
    }

    private void requireMembership(Long workspaceId) {
        AuthenticatedUser user = currentUserOrNull();
        if (user == null || user.admin()) {
            return;
        }
        if (!workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, user.id())) {
            throw new ForbiddenException(WorkspaceErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private static AuthenticatedUser currentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }
}
