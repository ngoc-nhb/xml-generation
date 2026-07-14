package com.company.xmlgen.workspace.service;

import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.authentication.exception.UserErrorCode;
import com.company.xmlgen.authentication.repository.UserRepository;
import com.company.xmlgen.authentication.service.AdminAuthorizationGuard;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.exception.ValidationException;
import com.company.xmlgen.workspace.domain.WorkspacePermission;
import com.company.xmlgen.workspace.dto.request.AssignUserWorkspacesRequest;
import com.company.xmlgen.workspace.dto.request.WorkspaceMembershipAssignment;
import com.company.xmlgen.workspace.dto.response.UserWorkspaceResponse;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceMemberEntity;
import com.company.xmlgen.workspace.entity.WorkspaceRole;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-managed assignment of users to global workspaces with membership permissions.
 *
 * <p>Personal workspace memberships are never assigned or removed here — they stay with the user.
 */
@Service
public class UserWorkspaceServiceImpl implements UserWorkspaceService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final AdminAuthorizationGuard adminAuthorizationGuard;

    public UserWorkspaceServiceImpl(
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            AdminAuthorizationGuard adminAuthorizationGuard) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.adminAuthorizationGuard = adminAuthorizationGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserWorkspaceResponse> findByUser(Long userId) {
        adminAuthorizationGuard.requireAdmin();
        requireActiveUser(userId);
        return toResponses(workspaceMemberRepository.findByUser_Id(userId));
    }

    @Override
    @Transactional
    public List<UserWorkspaceResponse> assign(Long userId, AssignUserWorkspacesRequest request) {
        adminAuthorizationGuard.requireAdmin();
        UserEntity user = requireActiveUser(userId);

        Map<Long, Set<String>> permissionsByWorkspaceId = new HashMap<>();
        Set<Long> targetGlobalIds = new HashSet<>();
        for (WorkspaceMembershipAssignment membership : request.memberships()) {
            WorkspaceEntity workspace = workspaceRepository
                    .findByIdAndDeletedAtIsNull(membership.workspaceId())
                    .orElseThrow(() -> new NotFoundException(WorkspaceErrorCode.WORKSPACE_NOT_FOUND));
            if (workspace.getType() != WorkspaceType.GLOBAL) {
                throw new ValidationException(
                        WorkspaceErrorCode.INVALID_WORKSPACE_ASSIGNMENT,
                        "memberships",
                        "Only global workspaces can be assigned by an administrator.");
            }
            targetGlobalIds.add(workspace.getId());
            try {
                permissionsByWorkspaceId.put(
                        workspace.getId(), WorkspacePermission.normalizeCodes(membership.permissions()));
            } catch (IllegalArgumentException ex) {
                throw new ValidationException(
                        WorkspaceErrorCode.INVALID_WORKSPACE_PERMISSION,
                        "permissions",
                        "One or more permission codes are invalid.");
            }
        }

        List<WorkspaceMemberEntity> current = workspaceMemberRepository.findByUser_Id(userId);
        for (WorkspaceMemberEntity member : current) {
            WorkspaceEntity workspace = member.getWorkspace();
            if (workspace.getDeletedAt() != null) {
                continue;
            }
            if (workspace.getType() == WorkspaceType.PERSONAL) {
                continue;
            }
            if (!targetGlobalIds.contains(workspace.getId())) {
                workspaceMemberRepository.delete(member);
            }
        }

        Map<Long, WorkspaceMemberEntity> existingGlobal = new HashMap<>();
        for (WorkspaceMemberEntity member : current) {
            if (member.getWorkspace().getDeletedAt() == null
                    && member.getWorkspace().getType() == WorkspaceType.GLOBAL) {
                existingGlobal.put(member.getWorkspace().getId(), member);
            }
        }

        for (Long workspaceId : targetGlobalIds) {
            Set<String> permissions = permissionsByWorkspaceId.getOrDefault(workspaceId, Set.of());
            WorkspaceMemberEntity existing = existingGlobal.get(workspaceId);
            if (existing != null) {
                existing.setPermissionCodes(permissions);
                workspaceMemberRepository.save(existing);
            } else {
                WorkspaceEntity workspace = workspaceRepository.getReferenceById(workspaceId);
                WorkspaceMemberEntity created = new WorkspaceMemberEntity(
                        workspace, user, WorkspaceRole.WORKSPACE_USER, Instant.now());
                created.setPermissionCodes(permissions);
                workspaceMemberRepository.save(created);
            }
        }

        return toResponses(workspaceMemberRepository.findByUser_Id(userId));
    }

    private UserEntity requireActiveUser(Long userId) {
        return userRepository
                .findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));
    }

    private static List<UserWorkspaceResponse> toResponses(List<WorkspaceMemberEntity> members) {
        return members.stream()
                .filter(member -> member.getWorkspace().getDeletedAt() == null)
                .sorted(Comparator.comparing(member -> member.getWorkspace().getId()))
                .map(member -> new UserWorkspaceResponse(
                        member.getWorkspace().getId(),
                        member.getWorkspace().getCode(),
                        member.getWorkspace().getName(),
                        member.getWorkspace().getType(),
                        member.getRole(),
                        new ArrayList<>(member.getPermissionCodes())))
                .toList();
    }
}
