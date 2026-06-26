package com.company.xmlgen.template.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.service.TemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/{id}")
    public ApiResponse<TemplateResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(templateService.findById(id));
    }
}
