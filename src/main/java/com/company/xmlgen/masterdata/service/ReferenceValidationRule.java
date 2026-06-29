package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Validates one-level Master Data record references.
 */
@Component
public class ReferenceValidationRule implements ValidationRule {

    private static final String REFERENCE_NOT_FOUND = "REFERENCE_NOT_FOUND";
    private static final String REFERENCE_NOT_FOUND_MESSAGE = "Referenced master data record does not exist.";

    private final MasterDataRecordRepository masterDataRecordRepository;

    public ReferenceValidationRule(MasterDataRecordRepository masterDataRecordRepository) {
        this.masterDataRecordRepository = masterDataRecordRepository;
    }

    @Override
    public int priority() {
        return 400;
    }

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        if (context.data() == null || !context.data().isObject()) {
            return List.of();
        }

        return context.fields().stream()
                .filter(field -> field.getMasterDataReferenceTypeId() != null)
                .filter(field -> context.data().has(field.getFieldName()))
                .filter(field -> referenceMissing(context, field))
                .map(field -> new ValidationError(
                        field.getFieldName(), REFERENCE_NOT_FOUND, REFERENCE_NOT_FOUND_MESSAGE))
                .toList();
    }

    private boolean referenceMissing(ValidationContext context, MasterDataFieldEntity field) {
        JsonNode value = context.data().get(field.getFieldName());
        if (value == null || value.isNull() || !value.canConvertToLong()) {
            return true;
        }

        return !masterDataRecordRepository.existsByIdAndMasterDataTypeId(
                value.asLong(), field.getMasterDataReferenceTypeId());
    }
}
