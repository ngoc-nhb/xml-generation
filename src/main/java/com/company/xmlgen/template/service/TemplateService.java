package com.company.xmlgen.template.service;

import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;

/**
 * Template lifecycle operations.
 *
 * @see docs/11-implementation-guide/template.md §4
 */
public interface TemplateService {

    CreateTemplateResponse create(CreateTemplateRequest request);
}
