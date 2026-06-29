package com.company.xmlgen.template.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Template module error codes.
 *
 * @see docs/06-api-design/p3_template-api.md §30
 */
public enum TemplateErrorCode implements ErrorCode {

    TEMPLATE_CODE_ALREADY_EXISTS,
    TEMPLATE_NOT_FOUND,
    TEMPLATE_FIELD_NAME_DUPLICATE,
    TEMPLATE_FIELD_NOT_FOUND,
    TEMPLATE_PARENT_FIELD_NOT_FOUND,
    TEMPLATE_PARENT_CYCLE,
    TEMPLATE_MAPPING_DUPLICATE,
    TEMPLATE_INVALID_HIERARCHY;

    @Override
    public String code() {
        return name();
    }
}
