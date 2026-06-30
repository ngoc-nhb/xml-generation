package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.service.RuntimeLoader;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;
import org.springframework.stereotype.Service;

/**
 * Coordinates Runtime Loader, Validation, Value Resolution, and XML Generation.
 */
@Service
public class RuntimeExecutionOrchestratorImpl implements RuntimeExecutionOrchestrator {

    private final RuntimeLoader runtimeLoader;
    private final RuntimeValidationService runtimeValidationService;
    private final ValueResolutionService valueResolutionService;
    private final XMLGenerationService xmlGenerationService;

    public RuntimeExecutionOrchestratorImpl(
            RuntimeLoader runtimeLoader,
            RuntimeValidationService runtimeValidationService,
            ValueResolutionService valueResolutionService,
            XMLGenerationService xmlGenerationService) {
        this.runtimeLoader = runtimeLoader;
        this.runtimeValidationService = runtimeValidationService;
        this.valueResolutionService = valueResolutionService;
        this.xmlGenerationService = xmlGenerationService;
    }

    @Override
    public RuntimeExecutionResult execute(RuntimeExecutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RuntimeExecutionRequest is required");
        }

        RuntimeTemplate runtimeTemplate = runtimeLoader.load(request.compiledSchemaJson());

        RuntimeValidationResult validationResult = runtimeValidationService.validate(runtimeTemplate);
        if (!validationResult.isValid()) {
            return RuntimeExecutionResult.validationFailed(validationResult);
        }

        ValueResolutionContext resolutionContext = new ValueResolutionContext(
                request.inputData(), request.selectedMasterData(), request.mappings());
        RuntimeExecutionTree executionTree = valueResolutionService.resolve(runtimeTemplate, resolutionContext);

        String xml = xmlGenerationService.generate(executionTree);
        return RuntimeExecutionResult.success(xml, executionTree);
    }
}
