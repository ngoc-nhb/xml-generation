package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.template.service.TemplateCompileMappingResolver;
import com.company.xmlgen.savedinput.service.SavedInputService;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import com.company.xmlgen.xmlgeneration.dto.ExportRequest;
import com.company.xmlgen.xmlgeneration.dto.ExportResponse;
import com.company.xmlgen.xmlgeneration.dto.ExportValidationError;
import com.company.xmlgen.xmlgeneration.exception.ExportErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Coordinates Template loading, runtime execution, and Export response mapping.
 */
@Service
public class ExportServiceImpl implements ExportService {

    private final TemplateRepository templateRepository;
    private final TemplateCompileMappingResolver templateCompileMappingResolver;
    private final SelectedMasterDataLoader selectedMasterDataLoader;
    private final RuntimeExecutionOrchestrator runtimeExecutionOrchestrator;
    private final WorkspaceOwnershipGuard workspaceOwnershipGuard;
    private final SavedInputService savedInputService;

    public ExportServiceImpl(
            TemplateRepository templateRepository,
            TemplateCompileMappingResolver templateCompileMappingResolver,
            SelectedMasterDataLoader selectedMasterDataLoader,
            RuntimeExecutionOrchestrator runtimeExecutionOrchestrator,
            WorkspaceOwnershipGuard workspaceOwnershipGuard,
            SavedInputService savedInputService) {
        this.templateRepository = templateRepository;
        this.templateCompileMappingResolver = templateCompileMappingResolver;
        this.selectedMasterDataLoader = selectedMasterDataLoader;
        this.runtimeExecutionOrchestrator = runtimeExecutionOrchestrator;
        this.workspaceOwnershipGuard = workspaceOwnershipGuard;
        this.savedInputService = savedInputService;
    }

    @Override
    public ExportResponse export(ExportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ExportRequest is required");
        }
        if (request.templateId() == null) {
            throw new IllegalArgumentException("templateId is required");
        }

        TemplateEntity template = workspaceOwnershipGuard.requireTemplate(request.templateId());

        JsonNode compiledSchemaJson = template.getCompiledSchemaJson();
        if (compiledSchemaJson == null || compiledSchemaJson.isNull()) {
            throw new BusinessException(ExportErrorCode.TEMPLATE_NOT_COMPILED, "Template has not been compiled");
        }

        List<TemplateCompileMapping> mappings = templateCompileMappingResolver.resolveByTemplateId(request.templateId());
        RuntimeExecutionRequest executionRequest = new RuntimeExecutionRequest(
                compiledSchemaJson,
                nullSafeInputData(request.inputData()),
                selectedMasterDataLoader.load(nullSafeJson(request.selectedMasterData())),
                mappings);

        RuntimeExecutionResult executionResult = runtimeExecutionOrchestrator.execute(executionRequest);
        ExportResponse response = toExportResponse(executionResult);
        if (response.successful()) {
            savedInputService.saveFromExport(
                    request.templateId(), request.inputData(), request.selectedMasterData());
        }
        return response;
    }

    private static ExportResponse toExportResponse(RuntimeExecutionResult executionResult) {
        if (!executionResult.isSuccessful()) {
            List<ExportValidationError> validationErrors = executionResult.validationResult().errors().stream()
                    .map(error -> new ExportValidationError(error.fieldName(), error.code(), error.message()))
                    .toList();
            return ExportResponse.validationFailed(validationErrors);
        }

        return ExportResponse.success(executionResult.xml());
    }

    private static JsonNode nullSafeInputData(JsonNode node) {
        return node == null || node.isNull() ? JsonNodeFactory.instance.objectNode() : node;
    }

    private static JsonNode nullSafeJson(JsonNode node) {
        return node == null || node.isNull() ? NullNode.instance : node;
    }
}
