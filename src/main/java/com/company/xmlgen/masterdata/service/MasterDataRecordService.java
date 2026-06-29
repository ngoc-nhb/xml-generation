package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordDetailResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordListResponse;

/**
 * Master Data Record business operations.
 */
public interface MasterDataRecordService {

    PageResult<MasterDataRecordListResponse> findAll(Long typeId, int page, int pageSize, String keyword);

    MasterDataRecordDetailResponse create(CreateMasterDataRecordRequest request);

    MasterDataRecordDetailResponse findById(Long id);

    MasterDataRecordDetailResponse update(Long id, UpdateMasterDataRecordRequest request);

    void delete(Long id);
}
