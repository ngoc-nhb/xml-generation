package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Export application-layer error codes.
 */
public enum ExportErrorCode implements ErrorCode {

    TEMPLATE_NOT_COMPILED;

    @Override
    public String code() {
        return name();
    }
}
