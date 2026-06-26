package com.company.xmlgen.template.service;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateListResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;

/**
 * Template lifecycle operations.
 *
 * @see docs/11-implementation-guide/template.md §4
 */
public interface TemplateService {

    CreateTemplateResponse create(CreateTemplateRequest request);

    TemplateResponse findById(Long id);

    PageResult<TemplateListResponse> findAll(int page, int pageSize, String keyword);
}
