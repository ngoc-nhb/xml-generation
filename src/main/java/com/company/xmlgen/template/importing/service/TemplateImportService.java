package com.company.xmlgen.template.importing.service;

import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Imports XML samples into non-persisted template drafts.
 */
public interface TemplateImportService {

    TemplateImportDraftResponse importXml(MultipartFile file);
}
