package com.company.xmlgen.masterdata.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordListResponse;
import com.company.xmlgen.masterdata.service.MasterDataRecordService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Master Data Record HTTP endpoints.
 */
@RestController
@RequestMapping("/api/v1/master-data")
public class MasterDataRecordController {

    private final MasterDataRecordService masterDataRecordService;

    public MasterDataRecordController(MasterDataRecordService masterDataRecordService) {
        this.masterDataRecordService = masterDataRecordService;
    }

    @GetMapping("/records")
    public ApiResponse<List<MasterDataRecordListResponse>> findAll(
            @RequestParam Long typeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        PageResult<MasterDataRecordListResponse> result =
                masterDataRecordService.findAll(typeId, page, pageSize, keyword);
        return ApiResponse.ok(result.content(), result.meta());
    }
}
