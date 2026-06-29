package com.company.xmlgen.template.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Error codes raised while parsing Template metadata into runtime schema.
 */
public enum TemplateSchemaParserErrorCode implements ErrorCode {

    TEMPLATE_SCHEMA_INVALID,
    TEMPLATE_FIELD_NAME_DUPLICATE,
    TEMPLATE_PARENT_FIELD_NOT_FOUND,
    TEMPLATE_PARENT_CYCLE;

    @Override
    public String code() {
        return name();
    }
}
