package com.company.xmlgen.masterdata.service;

/**
 * Validates Master Data Record values against their validation context.
 */
public interface MasterDataValidationService {

    ValidationResult validate(ValidationContext context);
}
