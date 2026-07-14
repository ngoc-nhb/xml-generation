package com.company.xmlgen.workspace.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.CommonErrorCode;
import com.company.xmlgen.exception.ForbiddenException;
import com.company.xmlgen.workspace.context.WorkspaceContextHolder;
import com.company.xmlgen.workspace.domain.WorkspacePermission;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Capability checks for the active workspace membership.
 *
 * <p>Allowed when the caller is a system admin, or the membership for
 * {@code current user + current workspace} contains the required permission code.
 * Roles are not treated as implicit capabilities.
 */
@Component
public class UserPermissionGuard {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public UserPermissionGuard(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public void requireTemplateImportPermission() {
        requirePermission(WorkspacePermission.IMPORT_TEMPLATE);
    }

    public void requireMasterDataWritePermission() {
        requirePermission(WorkspacePermission.MANAGE_MASTER_DATA);
    }

    public void requirePermission(WorkspacePermission permission) {
        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser.admin()) {
            return;
        }

        Long workspaceId = WorkspaceContextHolder.get()
                .map(context -> context.workspaceId())
                .orElseThrow(() -> new ForbiddenException(CommonErrorCode.FORBIDDEN));

        boolean allowed = workspaceMemberRepository
                .findByWorkspace_IdAndUser_Id(workspaceId, currentUser.id())
                .map(member -> member.hasPermission(permission))
                .orElse(false);

        if (!allowed) {
            throw new ForbiddenException(CommonErrorCode.FORBIDDEN);
        }
    }

    private static AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ForbiddenException(CommonErrorCode.FORBIDDEN);
        }
        return user;
    }
}
