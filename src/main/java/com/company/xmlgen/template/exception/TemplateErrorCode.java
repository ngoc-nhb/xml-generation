package com.company.xmlgen.template.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Template module error codes.
 *
 * @see docs/06-api-design/p3_template-api.md §30
 */
public enum TemplateErrorCode implements ErrorCode {

    TEMPLATE_CODE_ALREADY_EXISTS;

    @Override
    public String code() {
        return name();
    }
}
