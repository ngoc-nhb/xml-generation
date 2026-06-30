package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Preview application-layer error codes.
 */
public enum PreviewErrorCode implements ErrorCode {

    TEMPLATE_NOT_COMPILED;

    @Override
    public String code() {
        return name();
    }
}
