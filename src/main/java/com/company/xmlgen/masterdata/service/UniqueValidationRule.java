package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Validates uniqueness for configured Master Data fields.
 */
@Component
public class UniqueValidationRule implements ValidationRule {

    private static final String DUPLICATE_VALUE = "DUPLICATE_VALUE";
    private static final String DUPLICATE_VALUE_MESSAGE = "Value already exists.";

    private final MasterDataRecordRepository masterDataRecordRepository;

    public UniqueValidationRule(MasterDataRecordRepository masterDataRecordRepository) {
        this.masterDataRecordRepository = masterDataRecordRepository;
    }

    @Override
    public int priority() {
        return 300;
    }

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        if (context.data() == null || !context.data().isObject()) {
            return List.of();
        }

        return context.fields().stream()
                .filter(MasterDataFieldEntity::isUnique)
                .filter(field -> context.data().has(field.getFieldName()))
                .filter(field -> hasDuplicate(context, field))
                .map(field -> new ValidationError(field.getFieldName(), DUPLICATE_VALUE, DUPLICATE_VALUE_MESSAGE))
                .toList();
    }

    private boolean hasDuplicate(ValidationContext context, MasterDataFieldEntity field) {
        JsonNode value = context.data().get(field.getFieldName());
        return masterDataRecordRepository.existsDuplicateValue(
                context.typeId(),
                field.getFieldName(),
                value.toString(),
                context.recordId());
    }
}
