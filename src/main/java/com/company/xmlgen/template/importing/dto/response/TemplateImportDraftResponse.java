package com.company.xmlgen.template.importing.dto.response;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import java.util.List;

/**
 * Non-persisted template draft returned by {@code POST /api/v1/templates/import}.
 */
public record TemplateImportDraftResponse(
        String suggestedCode,
        String suggestedName,
        String sourceFileName,
        List<TemplateImportDraftFieldResponse> fields) {}
