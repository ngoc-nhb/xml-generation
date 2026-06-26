package com.company.xmlgen.authentication.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Authentication module error codes.
 *
 * @see docs/06-api-design/p2_authen-api.md §18
 */
public enum AuthenticationErrorCode implements ErrorCode {

    INVALID_CREDENTIALS;

    @Override
    public String code() {
        return name();
    }
}
