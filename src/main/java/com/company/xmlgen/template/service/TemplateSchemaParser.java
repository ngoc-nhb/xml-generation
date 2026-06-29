package com.company.xmlgen.template.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import java.util.List;

/**
 * Builds runtime schema models from already-loaded Template metadata.
 */
public interface TemplateSchemaParser {

    RuntimeTemplate parse(
            TemplateEntity template,
            List<TemplateFieldEntity> fields);
}
