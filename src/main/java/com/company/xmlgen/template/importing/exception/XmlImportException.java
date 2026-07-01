package com.company.xmlgen.template.importing.exception;

import com.company.xmlgen.exception.BusinessException;

/**
 * Raised when an XML sample cannot be imported into a template draft.
 */
public class XmlImportException extends BusinessException {

    public XmlImportException(XmlImportErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
