package com.company.xmlgen.template.service;

import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.exception.MasterDataFieldErrorCode;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileContext;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateMappingEntity;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateFieldRepository;
import com.company.xmlgen.template.repository.TemplateMappingRepository;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final MasterDataFieldRepository masterDataFieldRepository;
    private final MasterDataTypeRepository masterDataTypeRepository;
    private final TemplateSchemaParser templateSchemaParser;
    private final TemplateSchemaCompiler templateSchemaCompiler;

    public TemplateCompilationOrchestratorImpl(
            TemplateRepository templateRepository,
            TemplateFieldRepository templateFieldRepository,
            TemplateMappingRepository templateMappingRepository,
            MasterDataFieldRepository masterDataFieldRepository,
            MasterDataTypeRepository masterDataTypeRepository,
            TemplateSchemaParser templateSchemaParser,
            TemplateSchemaCompiler templateSchemaCompiler) {
        this.templateRepository = templateRepository;
        this.templateFieldRepository = templateFieldRepository;
        this.templateMappingRepository = templateMappingRepository;
        this.masterDataFieldRepository = masterDataFieldRepository;
        this.masterDataTypeRepository = masterDataTypeRepository;
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
        TemplateCompileContext compileContext =
                new TemplateCompileContext(resolveCompileMappings(fields, mappings));

        RuntimeTemplate runtimeTemplate = templateSchemaParser.parse(template, fields);
        JsonNode compiledSchema = templateSchemaCompiler.compile(runtimeTemplate, compileContext);

        template.setCompiledSchemaJson(compiledSchema);
        templateRepository.save(template);
    }

    private List<TemplateCompileMapping> resolveCompileMappings(
            List<TemplateFieldEntity> fields, List<TemplateMappingEntity> mappings) {
        Map<Long, String> fieldIdToName = new HashMap<>();
        for (TemplateFieldEntity field : fields) {
            fieldIdToName.put(field.getId(), field.getFieldName());
        }

        List<TemplateCompileMapping> compileMappings = new ArrayList<>();
        for (TemplateMappingEntity mapping : mappings) {
            String fieldName = fieldIdToName.get(mapping.getTemplateFieldId());
            if (fieldName == null) {
                continue;
            }

            String masterDataTypeCode = null;
            String masterDataFieldName = null;
            if (mapping.getMasterDataFieldId() != null) {
                MasterDataFieldEntity masterDataField = masterDataFieldRepository
                        .findById(mapping.getMasterDataFieldId())
                        .orElseThrow(() -> new NotFoundException(MasterDataFieldErrorCode.MASTER_DATA_FIELD_NOT_FOUND));
                MasterDataTypeEntity masterDataType = masterDataTypeRepository
                        .findById(masterDataField.getMasterDataTypeId())
                        .orElseThrow(() -> new NotFoundException(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND));
                masterDataTypeCode = masterDataType.getCode();
                masterDataFieldName = masterDataField.getFieldName();
            }

            compileMappings.add(new TemplateCompileMapping(fieldName, masterDataTypeCode, masterDataFieldName));
        }

        compileMappings.sort(Comparator.comparing(TemplateCompileMapping::fieldName));
        return compileMappings;
    }
}
