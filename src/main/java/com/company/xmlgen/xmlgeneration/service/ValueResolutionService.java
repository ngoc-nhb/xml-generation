package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;

/**
 * Resolves runtime template fields into execution values.
 */
public interface ValueResolutionService {

    RuntimeExecutionTree resolve(RuntimeTemplate runtimeTemplate, ValueResolutionContext context);
}
