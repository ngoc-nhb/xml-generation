package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * XML generation error codes.
 */
public enum XMLGenerationErrorCode implements ErrorCode {

    EXECUTION_TREE_REQUIRED,
    INVALID_EXECUTION_TREE,
    MASTER_DATA_NOT_FOUND,
    MASTER_DATA_TYPE_MISMATCH;

    @Override
    public String code() {
        return name();
    }
}
