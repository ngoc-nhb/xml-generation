package com.company.xmlgen.template.importing.service;

import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftFieldResponse;
import com.company.xmlgen.template.importing.domain.XmlImportNode;
import java.util.List;

/**
 * Converts a parsed {@link XmlImportNode} tree into template draft fields.
 */
public interface TemplateDraftBuilder {

    List<TemplateImportDraftFieldResponse> build(XmlImportNode root);
}
