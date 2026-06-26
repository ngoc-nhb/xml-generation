package com.company.xmlgen.masterdata.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.dto.response.CreateMasterDataTypeResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataTypeListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataTypeResponse;
import com.company.xmlgen.masterdata.dto.response.UpdateMasterDataTypeResponse;
import com.company.xmlgen.masterdata.service.MasterDataTypeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Master Data Type HTTP endpoints.
 *
 * @see docs/06-api-design/p4_master-data-api.md §32
 */
@RestController
@RequestMapping("/api/v1/master-data/types")
public class MasterDataTypeController {

    private final MasterDataTypeService masterDataTypeService;

    public MasterDataTypeController(MasterDataTypeService masterDataTypeService) {
        this.masterDataTypeService = masterDataTypeService;
    }

    @GetMapping
    public ApiResponse<List<MasterDataTypeListResponse>> findAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        PageResult<MasterDataTypeListResponse> result = masterDataTypeService.findAll(page, pageSize, keyword);
        return ApiResponse.ok(result.content(), result.meta());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateMasterDataTypeResponse> create(
            @Valid @RequestBody CreateMasterDataTypeRequest request) {
        return ApiResponse.ok(masterDataTypeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UpdateMasterDataTypeResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateMasterDataTypeRequest request) {
        return ApiResponse.ok(masterDataTypeService.update(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<MasterDataTypeResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(masterDataTypeService.findById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        masterDataTypeService.delete(id);
        return ApiResponse.success("Master data type deleted successfully.");
    }
}
