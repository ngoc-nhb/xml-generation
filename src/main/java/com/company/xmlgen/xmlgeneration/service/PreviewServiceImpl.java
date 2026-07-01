package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.template.service.TemplateCompileMappingResolver;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionNode;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;
import com.company.xmlgen.xmlgeneration.dto.PreviewExecutionNodeResponse;
import com.company.xmlgen.xmlgeneration.dto.PreviewRequest;
import com.company.xmlgen.xmlgeneration.dto.PreviewResponse;
import com.company.xmlgen.xmlgeneration.dto.PreviewValidationError;
import com.company.xmlgen.xmlgeneration.exception.PreviewErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Coordinates Template loading, runtime execution, and Preview response mapping.
 */
@Service
public class PreviewServiceImpl implements PreviewService {

    private final TemplateRepository templateRepository;
    private final TemplateCompileMappingResolver templateCompileMappingResolver;
    private final SelectedMasterDataLoader selectedMasterDataLoader;
    private final RuntimeExecutionOrchestrator runtimeExecutionOrchestrator;
    private final WorkspaceOwnershipGuard workspaceOwnershipGuard;

    public PreviewServiceImpl(
            TemplateRepository templateRepository,
            TemplateCompileMappingResolver templateCompileMappingResolver,
            SelectedMasterDataLoader selectedMasterDataLoader,
            RuntimeExecutionOrchestrator runtimeExecutionOrchestrator,
            WorkspaceOwnershipGuard workspaceOwnershipGuard) {
        this.templateRepository = templateRepository;
        this.templateCompileMappingResolver = templateCompileMappingResolver;
        this.selectedMasterDataLoader = selectedMasterDataLoader;
        this.runtimeExecutionOrchestrator = runtimeExecutionOrchestrator;
        this.workspaceOwnershipGuard = workspaceOwnershipGuard;
    }

    @Override
    public PreviewResponse preview(PreviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("PreviewRequest is required");
        }
        if (request.templateId() == null) {
            throw new IllegalArgumentException("templateId is required");
        }

        TemplateEntity template = workspaceOwnershipGuard.requireTemplate(request.templateId());

        JsonNode compiledSchemaJson = template.getCompiledSchemaJson();
        if (compiledSchemaJson == null || compiledSchemaJson.isNull()) {
            throw new BusinessException(
                    PreviewErrorCode.TEMPLATE_NOT_COMPILED, "Template has not been compiled");
        }

        List<TemplateCompileMapping> mappings = templateCompileMappingResolver.resolveByTemplateId(request.templateId());
        RuntimeExecutionRequest executionRequest = new RuntimeExecutionRequest(
                compiledSchemaJson,
                nullSafeInputData(request.inputData()),
                selectedMasterDataLoader.load(nullSafeJson(request.selectedMasterData())),
                mappings);

        RuntimeExecutionResult executionResult = runtimeExecutionOrchestrator.execute(executionRequest);
        return toPreviewResponse(executionResult);
    }

    private static PreviewResponse toPreviewResponse(RuntimeExecutionResult executionResult) {
        if (!executionResult.isSuccessful()) {
            List<PreviewValidationError> validationErrors = executionResult.validationResult().errors().stream()
                    .map(error -> new PreviewValidationError(error.fieldName(), error.code(), error.message()))
                    .toList();
            return PreviewResponse.validationFailed(validationErrors);
        }

        List<PreviewExecutionNodeResponse> executionTree = executionResult.executionTree() == null
                ? List.of()
                : mapExecutionTree(executionResult.executionTree());
        return PreviewResponse.success(executionResult.xml(), executionTree);
    }

    private static List<PreviewExecutionNodeResponse> mapExecutionTree(RuntimeExecutionTree executionTree) {
        return executionTree.roots().stream().map(PreviewServiceImpl::mapExecutionNode).toList();
    }

    private static PreviewExecutionNodeResponse mapExecutionNode(RuntimeExecutionNode node) {
        String value = node.value() == null || node.value().isNull() ? null : node.value().asText();
        List<PreviewExecutionNodeResponse> children =
                node.children().stream().map(PreviewServiceImpl::mapExecutionNode).toList();
        return new PreviewExecutionNodeResponse(
                node.field().fieldName(), node.field().xmlName(), value, children);
    }

    private static JsonNode nullSafeInputData(JsonNode node) {
        return node == null || node.isNull() ? JsonNodeFactory.instance.objectNode() : node;
    }

    private static JsonNode nullSafeJson(JsonNode node) {
        return node == null || node.isNull() ? NullNode.instance : node;
    }
}
