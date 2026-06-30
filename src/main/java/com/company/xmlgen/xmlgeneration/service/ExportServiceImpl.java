package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.template.service.TemplateCompileMappingResolver;
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
    private final RuntimeExecutionOrchestrator runtimeExecutionOrchestrator;

    public ExportServiceImpl(
            TemplateRepository templateRepository,
            TemplateCompileMappingResolver templateCompileMappingResolver,
            RuntimeExecutionOrchestrator runtimeExecutionOrchestrator) {
        this.templateRepository = templateRepository;
        this.templateCompileMappingResolver = templateCompileMappingResolver;
        this.runtimeExecutionOrchestrator = runtimeExecutionOrchestrator;
    }

    @Override
    public ExportResponse export(ExportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ExportRequest is required");
        }
        if (request.templateId() == null) {
            throw new IllegalArgumentException("templateId is required");
        }

        TemplateEntity template = templateRepository
                .findById(request.templateId())
                .orElseThrow(() -> new NotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        JsonNode compiledSchemaJson = template.getCompiledSchemaJson();
        if (compiledSchemaJson == null || compiledSchemaJson.isNull()) {
            throw new BusinessException(ExportErrorCode.TEMPLATE_NOT_COMPILED, "Template has not been compiled");
        }

        List<TemplateCompileMapping> mappings = templateCompileMappingResolver.resolveByTemplateId(request.templateId());
        RuntimeExecutionRequest executionRequest = new RuntimeExecutionRequest(
                compiledSchemaJson,
                nullSafeInputData(request.inputData()),
                nullSafeJson(request.selectedMasterData()),
                mappings);

        RuntimeExecutionResult executionResult = runtimeExecutionOrchestrator.execute(executionRequest);
        return toExportResponse(executionResult);
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
