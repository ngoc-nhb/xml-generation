package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Error codes raised when runtime validation cannot be executed.
 */
public enum RuntimeValidationErrorCode implements ErrorCode {

    RUNTIME_TEMPLATE_REQUIRED;

    @Override
    public String code() {
        return name();
    }
}
