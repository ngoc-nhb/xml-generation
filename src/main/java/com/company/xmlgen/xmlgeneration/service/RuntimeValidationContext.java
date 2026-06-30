package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;

/**
 * Input context for runtime template validation.
 */
public record RuntimeValidationContext(RuntimeTemplate runtimeTemplate) {
}
