package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Loads master data records referenced by {@code { "TYPE_CODE": { "id": recordId } }} selections.
 *
 * <p>Already-expanded selections ({@code { "TYPE_CODE": { "field": value } }}) are passed through.
 */
@Service
public class SelectedMasterDataLoaderImpl implements SelectedMasterDataLoader {

    private final MasterDataRecordRepository masterDataRecordRepository;
    private final MasterDataTypeRepository masterDataTypeRepository;
    private final ObjectMapper objectMapper;

    public SelectedMasterDataLoaderImpl(
            MasterDataRecordRepository masterDataRecordRepository,
            MasterDataTypeRepository masterDataTypeRepository,
            ObjectMapper objectMapper) {
        this.masterDataRecordRepository = masterDataRecordRepository;
        this.masterDataTypeRepository = masterDataTypeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode load(JsonNode selectedMasterData) {
        if (selectedMasterData == null || selectedMasterData.isNull()) {
            return NullNode.instance;
        }
        if (!selectedMasterData.isObject()) {
            throw new BusinessException(
                    XMLGenerationErrorCode.MASTER_DATA_NOT_FOUND, "selectedMasterData must be an object");
        }

        ObjectNode resolved = objectMapper.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = selectedMasterData.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            resolved.set(entry.getKey(), resolveSelection(entry.getKey(), entry.getValue()));
        }
        return resolved;
    }

    private JsonNode resolveSelection(String typeCode, JsonNode selection) {
        if (selection == null || selection.isNull()) {
            return NullNode.instance;
        }
        if (isRecordReference(selection)) {
            return loadRecordData(typeCode, selection.get("id").asLong());
        }
        return selection;
    }

    private JsonNode loadRecordData(String typeCode, long recordId) {
        MasterDataRecordEntity record = masterDataRecordRepository
                .findById(recordId)
                .filter(entity -> entity.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException(
                        XMLGenerationErrorCode.MASTER_DATA_NOT_FOUND,
                        "Master data record not found: " + recordId));

        MasterDataTypeEntity type = masterDataTypeRepository
                .findById(record.getMasterDataTypeId())
                .orElseThrow(() -> new NotFoundException(
                        XMLGenerationErrorCode.MASTER_DATA_NOT_FOUND,
                        "Master data type not found for record: " + recordId));

        if (!type.getCode().equals(typeCode)) {
            throw new BusinessException(
                    XMLGenerationErrorCode.MASTER_DATA_TYPE_MISMATCH,
                    "Master data type mismatch for record "
                            + recordId
                            + ": expected "
                            + typeCode
                            + ", actual "
                            + type.getCode());
        }

        JsonNode dataJson = record.getDataJson();
        if (dataJson == null || dataJson.isNull()) {
            return objectMapper.createObjectNode();
        }
        return dataJson.deepCopy();
    }

    private static boolean isRecordReference(JsonNode selection) {
        if (!selection.isObject()) {
            return false;
        }
        JsonNode idNode = selection.get("id");
        if (idNode == null || !idNode.isIntegralNumber()) {
            return false;
        }
        Iterator<String> fieldNames = selection.fieldNames();
        return fieldNames.hasNext() && fieldNames.next().equals("id") && !fieldNames.hasNext();
    }
}
