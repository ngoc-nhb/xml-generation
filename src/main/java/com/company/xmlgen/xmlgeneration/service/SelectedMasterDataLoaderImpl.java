package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Loads master data records referenced by {@code { "TYPE_CODE": { "id": recordId } }} selections.
 *
 * <p>Already-expanded selections ({@code { "TYPE_CODE": { "field": value } }}) are passed through.
 *
 * <p>A repeatable group's selections are carried as an array aligned by index with the group's
 * input occurrences, e.g. {@code { "GameCategory": [ { "SEASON": { "id": 1 } }, { "SEASON": { "id": 2 } } ] } }.
 * Each array element is itself a {@code { "TYPE_CODE": ... } } scope and is resolved the same way
 * as the request root, so record references nested inside repeated groups are expanded per occurrence.
 */
@Service
public class SelectedMasterDataLoaderImpl implements SelectedMasterDataLoader {

    private final MasterDataRecordRepository masterDataRecordRepository;
    private final MasterDataTypeRepository masterDataTypeRepository;
    private final ObjectMapper objectMapper;
    private final WorkspaceOwnershipGuard workspaceOwnershipGuard;

    public SelectedMasterDataLoaderImpl(
            MasterDataRecordRepository masterDataRecordRepository,
            MasterDataTypeRepository masterDataTypeRepository,
            ObjectMapper objectMapper,
            WorkspaceOwnershipGuard workspaceOwnershipGuard) {
        this.masterDataRecordRepository = masterDataRecordRepository;
        this.masterDataTypeRepository = masterDataTypeRepository;
        this.objectMapper = objectMapper;
        this.workspaceOwnershipGuard = workspaceOwnershipGuard;
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

        return resolveScope(selectedMasterData);
    }

    /** Resolves one {@code { "TYPE_CODE": selection, ... } } scope — the request root, or one repeated-group occurrence. */
    private JsonNode resolveScope(JsonNode scope) {
        ObjectNode resolved = objectMapper.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = scope.fields();
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
        if (selection.isArray()) {
            ArrayNode resolvedOccurrences = objectMapper.createArrayNode();
            for (JsonNode occurrence : selection) {
                resolvedOccurrences.add(
                        occurrence != null && occurrence.isObject() ? resolveScope(occurrence) : occurrence);
            }
            return resolvedOccurrences;
        }
        return selection;
    }

    private JsonNode loadRecordData(String typeCode, long recordId) {
        var record = workspaceOwnershipGuard.requireMasterDataRecordForTypeCode(typeCode, recordId);

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
