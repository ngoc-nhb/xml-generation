package com.company.xmlgen.masterdata.service;

/**
 * Field-level validation error for Master Data Record validation.
 */
public record ValidationError(String field, String code, String message) {
}
