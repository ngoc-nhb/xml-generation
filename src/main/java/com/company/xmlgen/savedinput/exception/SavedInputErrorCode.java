package com.company.xmlgen.savedinput.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Saved Input module error codes.
 */
public enum SavedInputErrorCode implements ErrorCode {

    SAVED_INPUT_NOT_FOUND;

    @Override
    public String code() {
        return name();
    }
}
