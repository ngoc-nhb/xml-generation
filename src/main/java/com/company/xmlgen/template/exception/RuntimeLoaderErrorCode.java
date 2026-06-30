package com.company.xmlgen.template.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Error codes raised while loading compiled schema JSON into a runtime model.
 */
public enum RuntimeLoaderErrorCode implements ErrorCode {

    COMPILED_SCHEMA_INVALID,
    COMPILED_SCHEMA_FIELD_INVALID;

    @Override
    public String code() {
        return name();
    }
}
