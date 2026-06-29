package com.company.xmlgen.template.exception;

import com.company.xmlgen.exception.ApplicationException;

/**
 * Raised when Template metadata cannot be parsed into a runtime schema.
 */
public class TemplateSchemaParserException extends ApplicationException {

    private final TemplateSchemaParserErrorCode parserErrorCode;

    public TemplateSchemaParserException(TemplateSchemaParserErrorCode errorCode, String message) {
        super(errorCode, message);
        this.parserErrorCode = errorCode;
    }

    public TemplateSchemaParserErrorCode getParserErrorCode() {
        return parserErrorCode;
    }
}
