package com.company.xmlgen.workspace.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.ForbiddenException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.exception.ValidationException;
import com.company.xmlgen.workspace.domain.WorkspacePermission;
import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceMemberPermissionsRequest;
import com.company.xmlgen.workspace.dto.response.WorkspaceMemberResponse;
import com.company.xmlgen.workspace.entity.WorkspaceMemberEntity;
import com.company.xmlgen.workspace.entity.WorkspaceRole;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.mapper.WorkspaceMapper;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lists workspace members and updates membership capabilities.
 *
 * <p>Only system admins and {@code WORKSPACE_ADMIN} members may change permissions.
 */
@Service
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceMapper workspaceMapper;

    public WorkspaceMemberServiceImpl(
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            WorkspaceMapper workspaceMapper) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceMapper = workspaceMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> findByWorkspace(Long workspaceId) {
        requireActiveWorkspace(workspaceId);
        requireWorkspaceAdminOrSystemAdmin(workspaceId);
        return workspaceMemberRepository.findByWorkspace_Id(workspaceId).stream()
                .sorted(Comparator.comparing(member -> member.getUser().getUsername()))
                .map(workspaceMapper::toMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public WorkspaceMemberResponse updatePermissions(
            Long workspaceId, Long userId, UpdateWorkspaceMemberPermissionsRequest request) {
        requireActiveWorkspace(workspaceId);
        requireWorkspaceAdminOrSystemAdmin(workspaceId);

        WorkspaceMemberEntity membership = workspaceMemberRepository
                .findByWorkspace_IdAndUser_Id(workspaceId, userId)
                .orElseThrow(() -> new NotFoundException(WorkspaceErrorCode.WORKSPACE_MEMBER_NOT_FOUND));

        try {
            membership.setPermissionCodes(WorkspacePermission.normalizeCodes(request.permissions()));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(
                    WorkspaceErrorCode.INVALID_WORKSPACE_PERMISSION,
                    "permissions",
                    "One or more permission codes are invalid.");
        }
        return workspaceMapper.toMemberResponse(workspaceMemberRepository.save(membership));
    }

    private void requireActiveWorkspace(Long workspaceId) {
        workspaceRepository
                .findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new NotFoundException(WorkspaceErrorCode.WORKSPACE_NOT_FOUND));
    }

    private void requireWorkspaceAdminOrSystemAdmin(Long workspaceId) {
        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser.admin()) {
            return;
        }
        boolean workspaceAdmin = workspaceMemberRepository
                .findByWorkspace_IdAndUser_Id(workspaceId, currentUser.id())
                .map(member -> member.getRole() == WorkspaceRole.WORKSPACE_ADMIN)
                .orElse(false);
        if (!workspaceAdmin) {
            throw new ForbiddenException(WorkspaceErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    private AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
