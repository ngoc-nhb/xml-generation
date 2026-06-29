package com.company.xmlgen.masterdata.service;

import java.util.List;

/**
 * Extension point for Master Data Record validation rules.
 */
public interface ValidationRule {

    default int priority() {
        return 1_000;
    }

    List<ValidationError> validate(ValidationContext context);
}
