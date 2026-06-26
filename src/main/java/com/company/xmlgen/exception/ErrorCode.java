package com.company.xmlgen.exception;

/**
 * Marker interface for module-specific error code enumerations.
 * Each module defines its own enum implementing this interface
 * so error codes remain local to their owning module.
 */
public interface ErrorCode {
    String code();
}
