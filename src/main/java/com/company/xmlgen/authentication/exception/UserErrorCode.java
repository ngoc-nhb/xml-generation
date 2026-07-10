package com.company.xmlgen.authentication.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * User management module error codes.
 */
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND,
    USERNAME_ALREADY_EXISTS,
    PASSWORD_MISMATCH;

    @Override
    public String code() {
        return name();
    }
}
