package com.company.xmlgen.template.service;

import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.exception.MasterDataFieldErrorCode;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateMappingEntity;
import com.company.xmlgen.template.repository.TemplateFieldRepository;
import com.company.xmlgen.template.repository.TemplateMappingRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Loads template and master-data metadata required to build {@link TemplateCompileMapping} records.
 */
@Service
public class TemplateCompileMappingResolverImpl implements TemplateCompileMappingResolver {

    private final TemplateFieldRepository templateFieldRepository;
    private final TemplateMappingRepository templateMappingRepository;
    private final MasterDataFieldRepository masterDataFieldRepository;
    private final MasterDataTypeRepository masterDataTypeRepository;

    public TemplateCompileMappingResolverImpl(
            TemplateFieldRepository templateFieldRepository,
            TemplateMappingRepository templateMappingRepository,
            MasterDataFieldRepository masterDataFieldRepository,
            MasterDataTypeRepository masterDataTypeRepository) {
        this.templateFieldRepository = templateFieldRepository;
        this.templateMappingRepository = templateMappingRepository;
        this.masterDataFieldRepository = masterDataFieldRepository;
        this.masterDataTypeRepository = masterDataTypeRepository;
    }

    @Override
    public List<TemplateCompileMapping> resolveByTemplateId(Long templateId) {
        List<TemplateFieldEntity> fields = templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(templateId);
        List<TemplateMappingEntity> mappings = templateMappingRepository.findAllByTemplateId(templateId);
        return resolve(fields, mappings);
    }

    @Override
    public List<TemplateCompileMapping> resolve(List<TemplateFieldEntity> fields, List<TemplateMappingEntity> mappings) {
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
