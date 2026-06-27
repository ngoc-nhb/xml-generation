package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordListResponse;
import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
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

    private final MasterDataRecordRepository masterDataRecordRepository;
    private final ObjectMapper objectMapper;

    public MasterDataRecordServiceImpl(
            MasterDataRecordRepository masterDataRecordRepository, ObjectMapper objectMapper) {
        this.masterDataRecordRepository = masterDataRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MasterDataRecordListResponse> findAll(
            Long typeId, int page, int pageSize, String keyword) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedPageSize, Sort.by("id").ascending());

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

    private Map<String, Object> toValues(JsonNode dataJson) {
        return objectMapper.convertValue(dataJson, new TypeReference<>() {});
    }

    private static boolean isBlank(String keyword) {
        return keyword == null || keyword.isBlank();
    }
}
