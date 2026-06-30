package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Error codes raised when XML generation cannot be executed.
 */
public enum XMLGenerationErrorCode implements ErrorCode {

    EXECUTION_TREE_REQUIRED,
    INVALID_EXECUTION_TREE;

    @Override
    public String code() {
        return name();
    }
}
