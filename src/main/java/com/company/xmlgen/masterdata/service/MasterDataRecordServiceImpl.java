package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ErrorCode;
import com.company.xmlgen.exception.FieldViolation;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.exception.ValidationException;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordDetailResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordListResponse;
import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.workspace.service.UserPermissionGuard;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages Master Data Record business rules.
 */
@Service
public class MasterDataRecordServiceImpl implements MasterDataRecordService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final ErrorCode MASTER_DATA_RECORD_NOT_FOUND = () -> "MASTER_DATA_RECORD_NOT_FOUND";

    private final MasterDataRecordRepository masterDataRecordRepository;
    private final MasterDataTypeRepository masterDataTypeRepository;
    private final MasterDataValidationService masterDataValidationService;
    private final ObjectMapper objectMapper;
    private final WorkspaceOwnershipGuard workspaceOwnershipGuard;
    private final UserPermissionGuard userPermissionGuard;

    public MasterDataRecordServiceImpl(
            MasterDataRecordRepository masterDataRecordRepository,
            MasterDataTypeRepository masterDataTypeRepository,
            MasterDataValidationService masterDataValidationService,
            ObjectMapper objectMapper,
            WorkspaceOwnershipGuard workspaceOwnershipGuard,
            UserPermissionGuard userPermissionGuard) {
        this.masterDataRecordRepository = masterDataRecordRepository;
        this.masterDataTypeRepository = masterDataTypeRepository;
        this.masterDataValidationService = masterDataValidationService;
        this.objectMapper = objectMapper;
        this.workspaceOwnershipGuard = workspaceOwnershipGuard;
        this.userPermissionGuard = userPermissionGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MasterDataRecordListResponse> findAll(
            Long typeId, int page, int pageSize, String keyword) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedPageSize, Sort.by("id").ascending());

        workspaceOwnershipGuard.requireMasterDataType(typeId);

        String normalizedKeyword = isBlank(keyword) ? null : keyword.trim();
        Page<MasterDataRecordEntity> entityPage =
                masterDataRecordRepository.search(typeId, normalizedKeyword, pageable);

        List<MasterDataRecordListResponse> content = entityPage.getContent().stream()
                .map(entity -> new MasterDataRecordListResponse(
                        entity.getId(), entity.getMasterDataTypeId(), toValues(entity.getDataJson())))
                .toList();

        PageMeta meta = new PageMeta(
                normalizedPage,
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages());

        return new PageResult<>(content, meta);
    }

    @Override
    @Transactional
    public MasterDataRecordDetailResponse create(CreateMasterDataRecordRequest request) {
        userPermissionGuard.requireMasterDataWritePermission();
        workspaceOwnershipGuard.requireMasterDataType(request.typeId());

        validate(ValidationContext.create(request.typeId(), request.data()));

        MasterDataRecordEntity entity = new MasterDataRecordEntity(request.typeId(), request.data());
        MasterDataRecordEntity saved = masterDataRecordRepository.save(entity);

        return toDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MasterDataRecordDetailResponse findById(Long id) {
        MasterDataRecordEntity entity = workspaceOwnershipGuard.requireMasterDataRecord(id);

        return toDetailResponse(entity);
    }

    @Override
    @Transactional
    public MasterDataRecordDetailResponse update(Long id, UpdateMasterDataRecordRequest request) {
        userPermissionGuard.requireMasterDataWritePermission();
        MasterDataRecordEntity entity = workspaceOwnershipGuard.requireMasterDataRecord(id);

        validate(ValidationContext.update(id, entity.getMasterDataTypeId(), request.data()));

        entity.setDataJson(request.data());
        MasterDataRecordEntity saved = masterDataRecordRepository.save(entity);

        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userPermissionGuard.requireMasterDataWritePermission();
        MasterDataRecordEntity entity = workspaceOwnershipGuard.requireMasterDataRecord(id);

        masterDataRecordRepository.delete(entity);
    }

    private Map<String, Object> toValues(JsonNode dataJson) {
        return objectMapper.convertValue(dataJson, new TypeReference<>() {});
    }

    private void validate(ValidationContext context) {
        ValidationResult result = masterDataValidationService.validate(context);
        if (!result.isValid()) {
            throw new ValidationException(result.errors().stream()
                    .map(error -> FieldViolation.of(error.field(), error.code(), error.message()))
                    .toList());
        }
    }

    private static MasterDataRecordDetailResponse toDetailResponse(MasterDataRecordEntity entity) {
        return new MasterDataRecordDetailResponse(
                entity.getId(),
                entity.getMasterDataTypeId(),
                entity.getDataJson(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private static boolean isBlank(String keyword) {
        return keyword == null || keyword.isBlank();
    }
}
