package com.company.xmlgen.template.exception;

import com.company.xmlgen.exception.ApplicationException;

/**
 * Raised when compiled schema JSON cannot be loaded into a runtime schema model.
 */
public class RuntimeLoaderException extends ApplicationException {

    private final RuntimeLoaderErrorCode loaderErrorCode;

    public RuntimeLoaderException(RuntimeLoaderErrorCode errorCode, String message) {
        super(errorCode, message);
        this.loaderErrorCode = errorCode;
    }

    public RuntimeLoaderErrorCode getLoaderErrorCode() {
        return loaderErrorCode;
    }
}
