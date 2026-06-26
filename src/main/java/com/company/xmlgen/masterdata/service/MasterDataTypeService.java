package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.dto.response.CreateMasterDataTypeResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataTypeListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataTypeResponse;
import com.company.xmlgen.masterdata.dto.response.UpdateMasterDataTypeResponse;

/**
 * Master Data Type lifecycle operations.
 *
 * @see docs/11-implementation-guide/master-data.md
 */
public interface MasterDataTypeService {

    PageResult<MasterDataTypeListResponse> findAll(int page, int pageSize, String keyword);

    MasterDataTypeResponse findById(Long id);

    CreateMasterDataTypeResponse create(CreateMasterDataTypeRequest request);

    UpdateMasterDataTypeResponse update(Long id, UpdateMasterDataTypeRequest request);

    void delete(Long id);
}
