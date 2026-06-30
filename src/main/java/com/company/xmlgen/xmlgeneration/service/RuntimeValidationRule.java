package com.company.xmlgen.xmlgeneration.service;

import java.util.List;

/**
 * Extension point for runtime template validation rules.
 */
public interface RuntimeValidationRule {

    default int priority() {
        return 1_000;
    }

    List<RuntimeValidationError> validate(RuntimeValidationContext context);
}
