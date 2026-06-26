package com.company.xmlgen.template.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.TemplateSchemaRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateListResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateSchemaResponse;
import com.company.xmlgen.template.service.TemplateService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Template HTTP endpoints.
 *
 * @see docs/06-api-design/p3_template-api.md §23
 */
@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<List<TemplateListResponse>> findAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        PageResult<TemplateListResponse> result = templateService.findAll(page, pageSize, keyword);
        return ApiResponse.ok(result.content(), result.meta());
    }

    @PostMapping
    public ApiResponse<CreateTemplateResponse> create(@Valid @RequestBody CreateTemplateRequest request) {
        return ApiResponse.ok(templateService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TemplateResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(templateService.findById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TemplateResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateTemplateRequest request) {
        return ApiResponse.ok(templateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/schema")
    public ApiResponse<TemplateSchemaResponse> updateSchema(
            @PathVariable Long id, @Valid @RequestBody TemplateSchemaRequest request) {
        return ApiResponse.ok(templateService.updateSchema(id, request));
    }
}
