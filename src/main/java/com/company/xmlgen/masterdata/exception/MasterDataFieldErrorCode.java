package com.company.xmlgen.masterdata.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Master Data Field module error codes.
 */
public enum MasterDataFieldErrorCode implements ErrorCode {

    MASTER_DATA_FIELD_NOT_FOUND,
    MASTER_DATA_FIELD_CODE_ALREADY_EXISTS,
    DISPLAY_ORDER_ALREADY_EXISTS;

    @Override
    public String code() {
        return name();
    }
}
