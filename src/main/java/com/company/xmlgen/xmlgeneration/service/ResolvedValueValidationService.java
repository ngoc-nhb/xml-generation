package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;

/**
 * Validates resolved execution values after value resolution.
 */
public interface ResolvedValueValidationService {

    RuntimeValidationResult validate(RuntimeExecutionTree executionTree);
}
