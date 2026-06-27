package com.company.xmlgen.template.service;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateListResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateSchemaResponse;
import com.company.xmlgen.template.dto.response.UpdateTemplateResponse;
import com.company.xmlgen.template.entity.TemplateStatus;

/**
 * Template lifecycle operations.
 *
 * @see docs/11-implementation-guide/template.md §4
 */
public interface TemplateService {

    CreateTemplateResponse create(CreateTemplateRequest request);

    UpdateTemplateResponse update(Long id, UpdateTemplateRequest request);

    TemplateResponse findById(Long id);

    void delete(Long id);

    TemplateSchemaResponse updateSchema(Long id, UpdateTemplateSchemaRequest request);

    PageResult<TemplateListResponse> findAll(int page, int pageSize, String keyword, TemplateStatus status);
}
