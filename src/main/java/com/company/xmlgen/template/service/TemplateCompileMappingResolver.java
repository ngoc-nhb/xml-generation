package com.company.xmlgen.template.service;

import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateMappingEntity;
import java.util.List;

/**
 * Resolves template mapping metadata into {@link TemplateCompileMapping} records.
 *
 * <p>Application-layer component shared by compile orchestration and runtime execution
 * services (Preview, Export).
 */
public interface TemplateCompileMappingResolver {

    List<TemplateCompileMapping> resolveByTemplateId(Long templateId);

    List<TemplateCompileMapping> resolve(List<TemplateFieldEntity> fields, List<TemplateMappingEntity> mappings);
}
