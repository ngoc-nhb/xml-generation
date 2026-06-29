package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.masterdata.entity.MasterDataFieldDataType;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Validates JSON value types against Master Data field definitions.
 */
@Component
public class DataTypeValidationRule implements ValidationRule {

    private static final String INVALID_DATA_TYPE = "INVALID_DATA_TYPE";

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public List<ValidationError> validate(ValidationContext context) {
        if (context.data() == null || !context.data().isObject()) {
            return List.of();
        }

        Map<String, MasterDataFieldEntity> fieldsByName = context.fields().stream()
                .collect(Collectors.toMap(
                        MasterDataFieldEntity::getFieldName,
                        Function.identity(),
                        (existing, replacement) -> existing));

        return context.data().properties().stream()
                .filter(entry -> fieldsByName.containsKey(entry.getKey()))
                .filter(entry -> !isValidType(fieldsByName.get(entry.getKey()).getDataType(), entry.getValue()))
                .map(entry -> invalidType(entry.getKey(), fieldsByName.get(entry.getKey()).getDataType()))
                .toList();
    }

    private static ValidationError invalidType(String fieldName, MasterDataFieldDataType dataType) {
        return new ValidationError(fieldName, INVALID_DATA_TYPE, "Expected " + dataType.name() + ".");
    }

    private static boolean isValidType(MasterDataFieldDataType dataType, JsonNode value) {
        if (value == null || value.isNull()) {
            return true;
        }

        return switch (dataType) {
            case STRING -> value.isTextual();
            case INTEGER, LONG -> value.isIntegralNumber();
            case DECIMAL -> value.isNumber();
            case BOOLEAN -> value.isBoolean();
            case DATE -> isValidDate(value);
            case DATETIME -> isValidDateTime(value);
        };
    }

    private static boolean isValidDate(JsonNode value) {
        if (!value.isTextual()) {
            return false;
        }

        try {
            LocalDate.parse(value.asText(), DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private static boolean isValidDateTime(JsonNode value) {
        if (!value.isTextual()) {
            return false;
        }

        try {
            DateTimeFormatter.ISO_DATE_TIME.parse(value.asText());
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
