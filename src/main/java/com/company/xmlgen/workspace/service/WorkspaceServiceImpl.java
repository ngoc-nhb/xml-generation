package com.company.xmlgen.workspace.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.authentication.repository.UserRepository;
import com.company.xmlgen.authentication.service.AdminAuthorizationGuard;
import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ForbiddenException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.workspace.domain.WorkspacePermission;
import com.company.xmlgen.workspace.dto.request.CreatePersonalWorkspaceRequest;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.UpdateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceListResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceResponse;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceMemberEntity;
import com.company.xmlgen.workspace.entity.WorkspaceRole;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.mapper.WorkspaceMapper;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages Workspace business rules.
 *
 * <p>{@code POST /workspaces} creates GLOBAL workspaces (system admin only). Personal workspaces
 * are created via {@link #createPersonal}.
 */
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceValidator workspaceValidator;
    private final AdminAuthorizationGuard adminAuthorizationGuard;
    private final PersonalWorkspaceService personalWorkspaceService;

    public WorkspaceServiceImpl(
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            UserRepository userRepository,
            WorkspaceMapper workspaceMapper,
            WorkspaceValidator workspaceValidator,
            AdminAuthorizationGuard adminAuthorizationGuard,
            PersonalWorkspaceService personalWorkspaceService) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.workspaceMapper = workspaceMapper;
        this.workspaceValidator = workspaceValidator;
        this.adminAuthorizationGuard = adminAuthorizationGuard;
        this.personalWorkspaceService = personalWorkspaceService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WorkspaceListResponse> findAll(int page, int pageSize) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Pageable pageable =
                PageRequest.of(normalizedPage - 1, normalizedPageSize, Sort.by("id").ascending());

        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser.admin()) {
            Page<WorkspaceEntity> entityPage = workspaceRepository.findByDeletedAtIsNull(pageable);
            List<WorkspaceListResponse> content = entityPage.getContent().stream()
                    .map(entity -> workspaceMapper.toListResponse(entity, null, null))
                    .toList();
            return new PageResult<>(content, toPageMeta(normalizedPage, entityPage));
        }

        Map<Long, WorkspaceMemberEntity> membershipByWorkspaceId = workspaceMemberRepository
                .findByUser_Id(currentUser.id())
                .stream()
                .collect(Collectors.toMap(member -> member.getWorkspace().getId(), member -> member));

        if (membershipByWorkspaceId.isEmpty()) {
            return new PageResult<>(List.of(), new PageMeta(normalizedPage, normalizedPageSize, 0, 0));
        }

        Page<WorkspaceEntity> entityPage =
                workspaceRepository.findByDeletedAtIsNullAndIdIn(membershipByWorkspaceId.keySet(), pageable);
        List<WorkspaceListResponse> content = entityPage.getContent().stream()
                .map(entity -> {
                    WorkspaceMemberEntity membership = membershipByWorkspaceId.get(entity.getId());
                    return workspaceMapper.toListResponse(
                            entity, membership.getRole(), membership.getPermissionCodes());
                })
                .toList();
        return new PageResult<>(content, toPageMeta(normalizedPage, entityPage));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse findById(Long id) {
        WorkspaceEntity entity = findActiveWorkspace(id);
        requireMemberOrAdmin(id);
        return workspaceMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CreateWorkspaceResponse create(CreateWorkspaceRequest request) {
        adminAuthorizationGuard.requireAdmin();
        workspaceValidator.validateCreate(request);

        AuthenticatedUser currentUser = getCurrentUser();
        WorkspaceEntity entity = new WorkspaceEntity(
                request.code().trim(),
                request.name().trim(),
                request.status(),
                WorkspaceType.GLOBAL,
                currentUser.id());
        entity.setDescription(request.description());

        WorkspaceEntity saved = workspaceRepository.save(entity);

        UserEntity creator = userRepository.getReferenceById(currentUser.id());
        WorkspaceMemberEntity membership =
                new WorkspaceMemberEntity(saved, creator, WorkspaceRole.WORKSPACE_ADMIN, Instant.now());
        membership.setPermissionCodes(new LinkedHashSet<>(WorkspacePermission.allCodes()));
        workspaceMemberRepository.save(membership);

        return workspaceMapper.toCreateResponse(saved);
    }

    @Override
    @Transactional
    public CreateWorkspaceResponse createPersonal(CreatePersonalWorkspaceRequest request) {
        return personalWorkspaceService.createForCurrentUser(request == null ? null : request.name());
    }

    @Override
    @Transactional
    public UpdateWorkspaceResponse update(Long id, UpdateWorkspaceRequest request) {
        WorkspaceEntity entity = findActiveWorkspace(id);
        requireWorkspaceAdminOrSystemAdmin(id);

        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setStatus(request.status());

        WorkspaceEntity saved = workspaceRepository.save(entity);
        return workspaceMapper.toUpdateResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WorkspaceEntity entity = findActiveWorkspace(id);
        requireWorkspaceAdminOrSystemAdmin(id);
        workspaceValidator.validateDeletable(id);

        entity.setDeletedAt(Instant.now());
        workspaceRepository.save(entity);
    }

    private void requireMemberOrAdmin(Long workspaceId) {
        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser.admin()) {
            return;
        }
        if (!workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, currentUser.id())) {
            throw new ForbiddenException(WorkspaceErrorCode.WORKSPACE_ACCESS_DENIED);
        }
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

    private static PageMeta toPageMeta(int page, Page<WorkspaceEntity> entityPage) {
        return new PageMeta(
                page, entityPage.getSize(), entityPage.getTotalElements(), entityPage.getTotalPages());
    }

    private WorkspaceEntity findActiveWorkspace(Long id) {
        return workspaceRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(WorkspaceErrorCode.WORKSPACE_NOT_FOUND));
    }

    private AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
