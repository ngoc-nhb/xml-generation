package com.company.xmlgen.xmlgeneration.service;

/**
 * Coordinates the runtime execution pipeline from compiled schema to XML.
 */
public interface RuntimeExecutionOrchestrator {

    RuntimeExecutionResult execute(RuntimeExecutionRequest request);
}
