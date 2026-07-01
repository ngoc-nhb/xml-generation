package com.company.xmlgen.workspace.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.UpdateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceListResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceResponse;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.mapper.WorkspaceMapper;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import java.time.Instant;
import java.util.List;
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
 * @see docs/02-domain-model/p5_workspace-ownership.md
 */
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceValidator workspaceValidator;

    public WorkspaceServiceImpl(
            WorkspaceRepository workspaceRepository,
            WorkspaceMapper workspaceMapper,
            WorkspaceValidator workspaceValidator) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMapper = workspaceMapper;
        this.workspaceValidator = workspaceValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WorkspaceListResponse> findAll(int page, int pageSize) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Pageable pageable =
                PageRequest.of(normalizedPage - 1, normalizedPageSize, Sort.by("id").ascending());

        Page<WorkspaceEntity> entityPage = workspaceRepository.findByDeletedAtIsNull(pageable);

        List<WorkspaceListResponse> content = entityPage.getContent().stream()
                .map(workspaceMapper::toListResponse)
                .toList();

        PageMeta meta = new PageMeta(
                normalizedPage,
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages());

        return new PageResult<>(content, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse findById(Long id) {
        WorkspaceEntity entity = findActiveWorkspace(id);
        return workspaceMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CreateWorkspaceResponse create(CreateWorkspaceRequest request) {
        workspaceValidator.validateCreate(request);

        AuthenticatedUser currentUser = getCurrentUser();
        WorkspaceEntity entity = new WorkspaceEntity(
                request.code().trim(), request.name().trim(), request.status(), currentUser.id());
        entity.setDescription(request.description());

        WorkspaceEntity saved = workspaceRepository.save(entity);
        return workspaceMapper.toCreateResponse(saved);
    }

    @Override
    @Transactional
    public UpdateWorkspaceResponse update(Long id, UpdateWorkspaceRequest request) {
        WorkspaceEntity entity = findActiveWorkspace(id);

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
        workspaceValidator.validateDeletable(id);

        entity.setDeletedAt(Instant.now());
        workspaceRepository.save(entity);
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
