package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;

/**
 * Validates a runtime template before value resolution or XML generation.
 */
public interface RuntimeValidationService {

    RuntimeValidationResult validate(RuntimeTemplate runtimeTemplate);
}
