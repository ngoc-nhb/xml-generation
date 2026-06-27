package com.company.xmlgen.masterdata.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordDetailResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordResponse;
import com.company.xmlgen.masterdata.dto.response.MessageResponse;
import com.company.xmlgen.masterdata.service.MasterDataRecordService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping("/records")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MasterDataRecordResponse> create(
            @Valid @RequestBody CreateMasterDataRecordRequest request) {
        return ApiResponse.ok(masterDataRecordService.create(request));
    }

    @GetMapping("/records/{id}")
    public ApiResponse<MasterDataRecordDetailResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(masterDataRecordService.findById(id));
    }

    @PutMapping("/records/{id}")
    public ApiResponse<MasterDataRecordDetailResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateMasterDataRecordRequest request) {
        return ApiResponse.ok(masterDataRecordService.update(id, request));
    }

    @DeleteMapping("/records/{id}")
    public ApiResponse<MessageResponse> delete(@PathVariable Long id) {
        masterDataRecordService.delete(id);
        return ApiResponse.ok(new MessageResponse("Master data record deleted successfully."));
    }
}
