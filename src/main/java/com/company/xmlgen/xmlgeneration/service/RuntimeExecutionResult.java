package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;

/**
 * Runtime Engine execution output from {@link RuntimeExecutionOrchestrator}.
 *
 * <p>Describes runtime execution results only. Do not add application-layer concerns such as
 * execution duration, database identifiers, HTTP status, Preview DTOs, or Export metadata.
 */
public final class RuntimeExecutionResult {

    private final boolean successful;
    private final String xml;
    private final RuntimeExecutionTree executionTree;
    private final RuntimeValidationResult validationResult;

    private RuntimeExecutionResult(
            boolean successful,
            String xml,
            RuntimeExecutionTree executionTree,
            RuntimeValidationResult validationResult) {
        this.successful = successful;
        this.xml = xml;
        this.executionTree = executionTree;
        this.validationResult = validationResult;
    }

    public static RuntimeExecutionResult success(String xml, RuntimeExecutionTree executionTree) {
        return new RuntimeExecutionResult(true, xml, executionTree, RuntimeValidationResult.valid());
    }

    public static RuntimeExecutionResult validationFailed(RuntimeValidationResult validationResult) {
        return new RuntimeExecutionResult(false, null, null, validationResult);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String xml() {
        return xml;
    }

    public RuntimeExecutionTree executionTree() {
        return executionTree;
    }

    public RuntimeValidationResult validationResult() {
        return validationResult;
    }
}
