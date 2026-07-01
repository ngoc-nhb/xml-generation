package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.dto.response.CreateMasterDataTypeResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataTypeListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataTypeResponse;
import com.company.xmlgen.masterdata.dto.response.UpdateMasterDataTypeResponse;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages Master Data Type business rules.
 *
 * @see docs/11-implementation-guide/master-data.md
 */
@Service
public class MasterDataTypeServiceImpl implements MasterDataTypeService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final MasterDataTypeRepository masterDataTypeRepository;
    private final WorkspaceOwnershipGuard workspaceOwnershipGuard;

    public MasterDataTypeServiceImpl(
            MasterDataTypeRepository masterDataTypeRepository, WorkspaceOwnershipGuard workspaceOwnershipGuard) {
        this.masterDataTypeRepository = masterDataTypeRepository;
        this.workspaceOwnershipGuard = workspaceOwnershipGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MasterDataTypeListResponse> findAll(int page, int pageSize, String keyword) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        long workspaceId = workspaceOwnershipGuard.currentWorkspaceId();

        Pageable pageable =
                PageRequest.of(normalizedPage - 1, normalizedPageSize, Sort.by("id").ascending());

        Page<MasterDataTypeEntity> entityPage = isBlank(keyword)
                ? masterDataTypeRepository.findByWorkspaceId(workspaceId, pageable)
                : masterDataTypeRepository.findByWorkspaceIdAndNameContainingIgnoreCase(
                        workspaceId, keyword.trim(), pageable);

        List<MasterDataTypeListResponse> content = entityPage.getContent().stream()
                .map(entity -> new MasterDataTypeListResponse(
                        entity.getId(), entity.getCode(), entity.getName(), entity.getStatus()))
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
    public MasterDataTypeResponse findById(Long id) {
        MasterDataTypeEntity entity = workspaceOwnershipGuard.requireMasterDataType(id);

        return new MasterDataTypeResponse(
                entity.getId(), entity.getCode(), entity.getName(), entity.getStatus());
    }

    @Override
    @Transactional
    public CreateMasterDataTypeResponse create(CreateMasterDataTypeRequest request) {
        long workspaceId = workspaceOwnershipGuard.currentWorkspaceId();
        if (masterDataTypeRepository.existsByWorkspaceIdAndCode(workspaceId, request.code())) {
            throw new ConflictException(MasterDataTypeErrorCode.MASTER_DATA_TYPE_CODE_ALREADY_EXISTS);
        }

        MasterDataTypeEntity entity =
                new MasterDataTypeEntity(request.code(), request.name(), request.status());
        entity.setDescription(request.description());
        entity.setWorkspaceId(workspaceId);

        MasterDataTypeEntity saved = masterDataTypeRepository.save(entity);

        return new CreateMasterDataTypeResponse(
                saved.getId(),
                saved.getCode(),
                saved.getName(),
                saved.getDescription(),
                saved.getStatus());
    }

    @Override
    @Transactional
    public UpdateMasterDataTypeResponse update(Long id, UpdateMasterDataTypeRequest request) {
        MasterDataTypeEntity entity = workspaceOwnershipGuard.requireMasterDataType(id);

        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setStatus(request.status());

        MasterDataTypeEntity saved = masterDataTypeRepository.save(entity);

        return new UpdateMasterDataTypeResponse(
                saved.getId(),
                saved.getCode(),
                saved.getName(),
                saved.getDescription(),
                saved.getStatus());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MasterDataTypeEntity entity = workspaceOwnershipGuard.requireMasterDataType(id);
        masterDataTypeRepository.delete(entity);
    }

    private static boolean isBlank(String keyword) {
        return keyword == null || keyword.isBlank();
    }
}
