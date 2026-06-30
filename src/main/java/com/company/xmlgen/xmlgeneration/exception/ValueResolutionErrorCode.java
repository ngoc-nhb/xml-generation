package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Error codes raised when value resolution cannot be executed.
 */
public enum ValueResolutionErrorCode implements ErrorCode {

    RUNTIME_TEMPLATE_REQUIRED,
    RESOLUTION_CONTEXT_REQUIRED;

    @Override
    public String code() {
        return name();
    }
}
