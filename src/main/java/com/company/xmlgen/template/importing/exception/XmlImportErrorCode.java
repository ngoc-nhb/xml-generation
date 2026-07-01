package com.company.xmlgen.template.importing.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Error codes for XML template import validation.
 */
public enum XmlImportErrorCode implements ErrorCode {
    XML_IMPORT_MALFORMED,
    XML_IMPORT_EMPTY,
    XML_IMPORT_MULTIPLE_ROOTS,
    XML_IMPORT_DUPLICATE_ATTRIBUTE,
    XML_IMPORT_UNSUPPORTED_CONSTRUCT;

    @Override
    public String code() {
        return name();
    }
}
