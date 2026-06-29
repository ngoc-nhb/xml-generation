package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Validates required Master Data fields.
 */
@Component
public class RequiredValidationRule implements ValidationRule {

    private static final String REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING";
    private static final String REQUIRED_FIELD_MESSAGE = "Field is required.";

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        return context.fields().stream()
                .filter(MasterDataFieldEntity::isRequired)
                .filter(field -> isMissingRequiredValue(context.data(), field.getFieldName()))
                .map(field -> new ValidationError(
                        field.getFieldName(), REQUIRED_FIELD_MISSING, REQUIRED_FIELD_MESSAGE))
                .toList();
    }

    private static boolean isMissingRequiredValue(JsonNode data, String fieldName) {
        if (data == null || !data.has(fieldName)) {
            return true;
        }

        JsonNode value = data.get(fieldName);
        if (value == null || value.isNull()) {
            return true;
        }

        return value.isTextual() && value.asText().trim().isEmpty();
    }
}
