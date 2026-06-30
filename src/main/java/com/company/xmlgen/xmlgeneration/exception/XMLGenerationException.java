package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ApplicationException;

/**
 * Raised when XML generation cannot be executed.
 */
public class XMLGenerationException extends ApplicationException {

    private final XMLGenerationErrorCode generationErrorCode;

    public XMLGenerationException(XMLGenerationErrorCode errorCode, String message) {
        super(errorCode, message);
        this.generationErrorCode = errorCode;
    }

    public XMLGenerationErrorCode getGenerationErrorCode() {
        return generationErrorCode;
    }

    public XMLGenerationException(XMLGenerationErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, null, message, cause);
        this.generationErrorCode = errorCode;
    }
}
