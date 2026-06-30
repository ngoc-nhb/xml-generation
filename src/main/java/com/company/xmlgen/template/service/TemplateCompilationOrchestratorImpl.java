package com.company.xmlgen.template.service;

import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileContext;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateMappingEntity;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateFieldRepository;
import com.company.xmlgen.template.repository.TemplateMappingRepository;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates metadata loading, mapping resolution, parsing, compilation, and persistence.
 */
@Service
public class TemplateCompilationOrchestratorImpl implements TemplateCompilationOrchestrator {

    private final TemplateRepository templateRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final TemplateMappingRepository templateMappingRepository;
    private final TemplateCompileMappingResolver templateCompileMappingResolver;
    private final TemplateSchemaParser templateSchemaParser;
    private final TemplateSchemaCompiler templateSchemaCompiler;

    public TemplateCompilationOrchestratorImpl(
            TemplateRepository templateRepository,
            TemplateFieldRepository templateFieldRepository,
            TemplateMappingRepository templateMappingRepository,
            TemplateCompileMappingResolver templateCompileMappingResolver,
            TemplateSchemaParser templateSchemaParser,
            TemplateSchemaCompiler templateSchemaCompiler) {
        this.templateRepository = templateRepository;
        this.templateFieldRepository = templateFieldRepository;
        this.templateMappingRepository = templateMappingRepository;
        this.templateCompileMappingResolver = templateCompileMappingResolver;
        this.templateSchemaParser = templateSchemaParser;
        this.templateSchemaCompiler = templateSchemaCompiler;
    }

    @Override
    @Transactional
    public void compileAndPersist(Long templateId) {
        TemplateEntity template = templateRepository
                .findById(templateId)
                .orElseThrow(() -> new NotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        List<TemplateFieldEntity> fields =
                templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(templateId);
        if (fields.isEmpty()) {
            template.setCompiledSchemaJson(null);
            templateRepository.save(template);
            return;
        }

        List<TemplateMappingEntity> mappings = templateMappingRepository.findAllByTemplateId(templateId);
        TemplateCompileContext compileContext = new TemplateCompileContext(
                templateCompileMappingResolver.resolve(fields, mappings));

        RuntimeTemplate runtimeTemplate = templateSchemaParser.parse(template, fields);
        JsonNode compiledSchema = templateSchemaCompiler.compile(runtimeTemplate, compileContext);

        template.setCompiledSchemaJson(compiledSchema);
        templateRepository.save(template);
    }
}
