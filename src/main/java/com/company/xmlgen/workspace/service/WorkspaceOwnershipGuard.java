package com.company.xmlgen.workspace.service;

import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.exception.MasterDataFieldErrorCode;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.workspace.context.WorkspaceContextHolder;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationErrorCode;
import org.springframework.stereotype.Component;

/**
 * Verifies that business resources belong to the active workspace from {@link WorkspaceContextHolder}.
 */
@Component
public class WorkspaceOwnershipGuard {

    private static final com.company.xmlgen.exception.ErrorCode MASTER_DATA_RECORD_NOT_FOUND =
            () -> "MASTER_DATA_RECORD_NOT_FOUND";

    private final TemplateRepository templateRepository;
    private final MasterDataTypeRepository masterDataTypeRepository;
    private final MasterDataFieldRepository masterDataFieldRepository;
    private final MasterDataRecordRepository masterDataRecordRepository;

    public WorkspaceOwnershipGuard(
            TemplateRepository templateRepository,
            MasterDataTypeRepository masterDataTypeRepository,
            MasterDataFieldRepository masterDataFieldRepository,
            MasterDataRecordRepository masterDataRecordRepository) {
        this.templateRepository = templateRepository;
        this.masterDataTypeRepository = masterDataTypeRepository;
        this.masterDataFieldRepository = masterDataFieldRepository;
        this.masterDataRecordRepository = masterDataRecordRepository;
    }

    public long currentWorkspaceId() {
        return WorkspaceContextHolder.require().workspaceId();
    }

    public TemplateEntity requireTemplate(Long templateId) {
        return templateRepository
                .findByIdAndWorkspaceId(templateId, currentWorkspaceId())
                .orElseThrow(() -> new NotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND));
    }

    public MasterDataTypeEntity requireMasterDataType(Long typeId) {
        return masterDataTypeRepository
                .findByIdAndWorkspaceId(typeId, currentWorkspaceId())
                .orElseThrow(() -> new NotFoundException(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND));
    }

    public MasterDataFieldEntity requireMasterDataField(Long fieldId) {
        MasterDataFieldEntity field = masterDataFieldRepository
                .findById(fieldId)
                .orElseThrow(() -> new NotFoundException(MasterDataFieldErrorCode.MASTER_DATA_FIELD_NOT_FOUND));
        requireMasterDataType(field.getMasterDataTypeId());
        return field;
    }

    public MasterDataRecordEntity requireMasterDataRecord(Long recordId) {
        MasterDataRecordEntity record = masterDataRecordRepository
                .findById(recordId)
                .filter(entity -> entity.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException(MASTER_DATA_RECORD_NOT_FOUND));
        requireMasterDataType(record.getMasterDataTypeId());
        return record;
    }

    public MasterDataRecordEntity requireMasterDataRecordForTypeCode(String typeCode, Long recordId) {
        MasterDataRecordEntity record = requireMasterDataRecord(recordId);
        MasterDataTypeEntity type = requireMasterDataType(record.getMasterDataTypeId());
        if (!type.getCode().equals(typeCode)) {
            throw new com.company.xmlgen.exception.BusinessException(
                    XMLGenerationErrorCode.MASTER_DATA_TYPE_MISMATCH,
                    "Master data type mismatch for record "
                            + recordId
                            + ": expected "
                            + typeCode
                            + ", actual "
                            + type.getCode());
        }
        return record;
    }
}
