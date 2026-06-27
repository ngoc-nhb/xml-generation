package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.response.CreateMasterDataFieldResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataFieldListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataFieldResponse;
import com.company.xmlgen.masterdata.dto.response.UpdateMasterDataFieldResponse;

/**
 * Master Data Field lifecycle operations.
 */
public interface MasterDataFieldService {

    PageResult<MasterDataFieldListResponse> findAll(
            Long typeId, int page, int pageSize, String keyword);

    MasterDataFieldResponse findById(Long id);

    CreateMasterDataFieldResponse create(CreateMasterDataFieldRequest request);

    UpdateMasterDataFieldResponse update(Long id, UpdateMasterDataFieldRequest request);

    void delete(Long id);
}
