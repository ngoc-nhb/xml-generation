package com.company.xmlgen.masterdata.service;

import java.util.List;

/**
 * Result of Master Data Record validation.
 */
public final class ValidationResult {

    private final boolean valid;
    private final List<ValidationError> errors;

    private ValidationResult(boolean valid, List<ValidationError> errors) {
        this.valid = valid;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult invalid(List<ValidationError> errors) {
        return new ValidationResult(false, errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<ValidationError> errors() {
        return errors;
    }
}
