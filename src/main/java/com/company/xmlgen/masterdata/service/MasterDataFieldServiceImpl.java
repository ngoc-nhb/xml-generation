package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.response.CreateMasterDataFieldResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataFieldListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataFieldResponse;
import com.company.xmlgen.masterdata.dto.response.UpdateMasterDataFieldResponse;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.exception.MasterDataFieldErrorCode;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages Master Data Field business rules.
 */
@Service
public class MasterDataFieldServiceImpl implements MasterDataFieldService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final MasterDataFieldRepository masterDataFieldRepository;
    private final MasterDataTypeRepository masterDataTypeRepository;

    public MasterDataFieldServiceImpl(
            MasterDataFieldRepository masterDataFieldRepository,
            MasterDataTypeRepository masterDataTypeRepository) {
        this.masterDataFieldRepository = masterDataFieldRepository;
        this.masterDataTypeRepository = masterDataTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MasterDataFieldListResponse> findAll(
            Long typeId, int page, int pageSize, String keyword) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(
                normalizedPage - 1, normalizedPageSize, Sort.by("displayOrder").ascending());

        Page<MasterDataFieldEntity> entityPage = findFieldPage(typeId, keyword, pageable);

        List<MasterDataFieldListResponse> content = mapToListResponses(entityPage.getContent());

        PageMeta meta = new PageMeta(
                normalizedPage,
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages());

        return new PageResult<>(content, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public MasterDataFieldResponse findById(Long id) {
        MasterDataFieldEntity entity = masterDataFieldRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(MasterDataFieldErrorCode.MASTER_DATA_FIELD_NOT_FOUND));

        return toDetailResponse(entity);
    }

    @Override
    @Transactional
    public CreateMasterDataFieldResponse create(CreateMasterDataFieldRequest request) {
        Long typeId = request.typeId();
        if (!masterDataTypeRepository.existsById(typeId)) {
            throw new NotFoundException(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND);
        }

        if (masterDataFieldRepository.existsByMasterDataTypeIdAndFieldName(typeId, request.code())) {
            throw new ConflictException(MasterDataFieldErrorCode.MASTER_DATA_FIELD_CODE_ALREADY_EXISTS);
        }

        if (masterDataFieldRepository.existsByMasterDataTypeIdAndDisplayOrder(typeId, request.displayOrder())) {
            throw new ConflictException(MasterDataFieldErrorCode.DISPLAY_ORDER_ALREADY_EXISTS);
        }

        MasterDataFieldEntity entity = new MasterDataFieldEntity(
                typeId,
                request.code(),
                request.name(),
                request.dataType(),
                request.required(),
                request.displayOrder());
        applyMetadata(entity, request);

        MasterDataFieldEntity saved = masterDataFieldRepository.save(entity);

        return new CreateMasterDataFieldResponse(
                saved.getId(),
                saved.getMasterDataTypeId(),
                saved.getFieldName(),
                saved.getName(),
                saved.getDataType(),
                saved.isRequired(),
                saved.getDisplayOrder(),
                saved.getDescription(),
                saved.getDefaultValue(),
                saved.isUnique(),
                saved.isSearchable(),
                saved.getMasterDataReferenceTypeId());
    }

    @Override
    @Transactional
    public UpdateMasterDataFieldResponse update(Long id, UpdateMasterDataFieldRequest request) {
        MasterDataFieldEntity entity = masterDataFieldRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(MasterDataFieldErrorCode.MASTER_DATA_FIELD_NOT_FOUND));

        if (masterDataFieldRepository.existsByMasterDataTypeIdAndDisplayOrderAndIdNot(
                entity.getMasterDataTypeId(), request.displayOrder(), id)) {
            throw new ConflictException(MasterDataFieldErrorCode.DISPLAY_ORDER_ALREADY_EXISTS);
        }

        entity.setName(request.name());
        entity.setDataType(request.dataType());
        entity.setRequired(request.required());
        entity.setDisplayOrder(request.displayOrder());
        applyMetadata(entity, request);

        MasterDataFieldEntity saved = masterDataFieldRepository.save(entity);

        MasterDataTypeEntity type = masterDataTypeRepository
                .findById(saved.getMasterDataTypeId())
                .orElseThrow(() -> new NotFoundException(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND));

        return new UpdateMasterDataFieldResponse(
                saved.getId(),
                saved.getMasterDataTypeId(),
                type.getCode(),
                type.getName(),
                saved.getFieldName(),
                saved.getName(),
                saved.getDataType(),
                saved.isRequired(),
                saved.getDisplayOrder(),
                saved.getDescription(),
                saved.getDefaultValue(),
                saved.isUnique(),
                saved.isSearchable(),
                saved.getMasterDataReferenceTypeId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MasterDataFieldEntity entity = masterDataFieldRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(MasterDataFieldErrorCode.MASTER_DATA_FIELD_NOT_FOUND));

        masterDataFieldRepository.delete(entity);
    }

    private Page<MasterDataFieldEntity> findFieldPage(Long typeId, String keyword, Pageable pageable) {
        if (typeId != null) {
            return isBlank(keyword)
                    ? masterDataFieldRepository.findByMasterDataTypeId(typeId, pageable)
                    : masterDataFieldRepository.findByMasterDataTypeIdAndNameContainingIgnoreCase(
                            typeId, keyword.trim(), pageable);
        }
        return isBlank(keyword)
                ? masterDataFieldRepository.findAll(pageable)
                : masterDataFieldRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    private List<MasterDataFieldListResponse> mapToListResponses(List<MasterDataFieldEntity> entities) {
        List<Long> typeIds =
                entities.stream().map(MasterDataFieldEntity::getMasterDataTypeId).distinct().toList();
        Map<Long, MasterDataTypeEntity> typesById = masterDataTypeRepository.findAllById(typeIds).stream()
                .collect(Collectors.toMap(MasterDataTypeEntity::getId, Function.identity()));

        return entities.stream()
                .map(entity -> {
                    MasterDataTypeEntity type = typesById.get(entity.getMasterDataTypeId());
                    return new MasterDataFieldListResponse(
                            entity.getId(),
                            entity.getMasterDataTypeId(),
                            type.getCode(),
                            type.getName(),
                            entity.getFieldName(),
                            entity.getName(),
                            entity.getDataType(),
                            entity.isRequired(),
                            entity.getDisplayOrder(),
                            entity.getDescription(),
                            entity.getDefaultValue(),
                            entity.isUnique(),
                            entity.isSearchable(),
                            entity.getMasterDataReferenceTypeId());
                })
                .toList();
    }

    private MasterDataFieldResponse toDetailResponse(MasterDataFieldEntity entity) {
        return new MasterDataFieldResponse(
                entity.getId(),
                entity.getMasterDataTypeId(),
                entity.getFieldName(),
                entity.getName(),
                entity.getDataType(),
                entity.isRequired(),
                entity.getDisplayOrder(),
                entity.getDescription(),
                entity.getDefaultValue(),
                entity.isUnique(),
                entity.isSearchable(),
                entity.getMasterDataReferenceTypeId());
    }

    private static void applyMetadata(MasterDataFieldEntity entity, CreateMasterDataFieldRequest request) {
        entity.setDescription(request.description());
        entity.setDefaultValue(request.defaultValue());
        entity.setUnique(request.unique() != null ? request.unique() : false);
        entity.setSearchable(request.searchable() != null ? request.searchable() : false);
        entity.setMasterDataReferenceTypeId(request.masterDataReferenceTypeId());
    }

    private static void applyMetadata(MasterDataFieldEntity entity, UpdateMasterDataFieldRequest request) {
        entity.setDescription(request.description());
        entity.setDefaultValue(request.defaultValue());
        entity.setUnique(request.unique());
        entity.setSearchable(request.searchable());
        entity.setMasterDataReferenceTypeId(request.masterDataReferenceTypeId());
    }

    private static boolean isBlank(String keyword) {
        return keyword == null || keyword.isBlank();
    }
}
