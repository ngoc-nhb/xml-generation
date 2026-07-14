package com.company.xmlgen.workspace.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.authentication.repository.UserRepository;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.workspace.domain.WorkspacePermission;
import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceMemberEntity;
import com.company.xmlgen.workspace.entity.WorkspaceRole;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.mapper.WorkspaceMapper;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import java.time.Instant;
import java.util.LinkedHashSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a user's personal workspace on explicit request (not during user creation).
 */
@Service
public class PersonalWorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;

    public PersonalWorkspaceService(
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            UserRepository userRepository,
            WorkspaceMapper workspaceMapper) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.workspaceMapper = workspaceMapper;
    }

    public static String personalCode(Long userId) {
        return "PERSONAL_" + userId;
    }

    public static String defaultPersonalName(String username) {
        return username + " Workspace";
    }

    @Transactional
    public CreateWorkspaceResponse createForCurrentUser(String requestedName) {
        AuthenticatedUser currentUser = getCurrentUser();
        if (workspaceRepository.existsByCreatedByIdAndTypeAndDeletedAtIsNull(
                currentUser.id(), WorkspaceType.PERSONAL)) {
            throw new ConflictException(WorkspaceErrorCode.PERSONAL_WORKSPACE_ALREADY_EXISTS);
        }

        UserEntity user = userRepository
                .findByIdAndDeletedAtIsNull(currentUser.id())
                .orElseThrow(() -> new NotFoundException(WorkspaceErrorCode.WORKSPACE_NOT_FOUND));

        String name = requestedName == null || requestedName.isBlank()
                ? defaultPersonalName(user.getUsername())
                : requestedName.trim();

        String code = personalCode(user.getId());
        if (workspaceRepository.existsByCode(code)) {
            throw new ConflictException(WorkspaceErrorCode.WORKSPACE_CODE_ALREADY_EXISTS);
        }

        WorkspaceEntity workspace = new WorkspaceEntity(
                code, name, WorkspaceStatus.ACTIVE, WorkspaceType.PERSONAL, user.getId());
        WorkspaceEntity saved = workspaceRepository.save(workspace);

        WorkspaceMemberEntity membership =
                new WorkspaceMemberEntity(saved, user, WorkspaceRole.WORKSPACE_ADMIN, Instant.now());
        membership.setPermissionCodes(new LinkedHashSet<>(WorkspacePermission.allCodes()));
        workspaceMemberRepository.save(membership);

        return workspaceMapper.toCreateResponse(saved);
    }

    private static AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
