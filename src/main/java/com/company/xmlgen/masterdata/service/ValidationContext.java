package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Input context for Master Data Record validation.
 */
public record ValidationContext(
        Long recordId,
        Long typeId,
        Operation operation,
        List<MasterDataFieldEntity> fields,
        JsonNode data) {

    public ValidationContext {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }

    public static ValidationContext create(Long typeId, JsonNode data) {
        return new ValidationContext(null, typeId, Operation.CREATE, List.of(), data);
    }

    public static ValidationContext update(Long recordId, Long typeId, JsonNode data) {
        return new ValidationContext(recordId, typeId, Operation.UPDATE, List.of(), data);
    }

    public ValidationContext withFields(List<MasterDataFieldEntity> fields) {
        return new ValidationContext(recordId, typeId, operation, fields, data);
    }

    public enum Operation {
        CREATE,
        UPDATE
    }
}
