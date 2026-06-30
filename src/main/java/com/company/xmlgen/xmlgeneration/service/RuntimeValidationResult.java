package com.company.xmlgen.xmlgeneration.service;

import java.util.List;

/**
 * Result of runtime template validation.
 */
public final class RuntimeValidationResult {

    private final boolean valid;
    private final List<RuntimeValidationError> errors;

    private RuntimeValidationResult(boolean valid, List<RuntimeValidationError> errors) {
        this.valid = valid;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static RuntimeValidationResult valid() {
        return new RuntimeValidationResult(true, List.of());
    }

    public static RuntimeValidationResult invalid(List<RuntimeValidationError> errors) {
        return new RuntimeValidationResult(false, errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<RuntimeValidationError> errors() {
        return errors;
    }
}
