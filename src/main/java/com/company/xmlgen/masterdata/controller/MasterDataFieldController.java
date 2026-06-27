package com.company.xmlgen.masterdata.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.response.CreateMasterDataFieldResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataFieldListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataFieldResponse;
import com.company.xmlgen.masterdata.dto.response.MessageResponse;
import com.company.xmlgen.masterdata.dto.response.UpdateMasterDataFieldResponse;
import com.company.xmlgen.masterdata.service.MasterDataFieldService;
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
 * Master Data Field HTTP endpoints.
 */
@RestController
@RequestMapping("/api/v1/master-data")
public class MasterDataFieldController {

    private final MasterDataFieldService masterDataFieldService;

    public MasterDataFieldController(MasterDataFieldService masterDataFieldService) {
        this.masterDataFieldService = masterDataFieldService;
    }

    @GetMapping("/fields")
    public ApiResponse<List<MasterDataFieldListResponse>> findAll(
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        PageResult<MasterDataFieldListResponse> result =
                masterDataFieldService.findAll(typeId, page, pageSize, keyword);
        return ApiResponse.ok(result.content(), result.meta());
    }

    @PostMapping("/fields")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateMasterDataFieldResponse> create(@Valid @RequestBody CreateMasterDataFieldRequest request) {
        return ApiResponse.ok(masterDataFieldService.create(request));
    }

    @GetMapping("/fields/{id}")
    public ApiResponse<MasterDataFieldResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(masterDataFieldService.findById(id));
    }

    @PutMapping("/fields/{id}")
    public ApiResponse<UpdateMasterDataFieldResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateMasterDataFieldRequest request) {
        return ApiResponse.ok(masterDataFieldService.update(id, request));
    }

    @DeleteMapping("/fields/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<MessageResponse> delete(@PathVariable Long id) {
        masterDataFieldService.delete(id);
        return ApiResponse.ok(new MessageResponse("Master data field deleted successfully."));
    }
}
