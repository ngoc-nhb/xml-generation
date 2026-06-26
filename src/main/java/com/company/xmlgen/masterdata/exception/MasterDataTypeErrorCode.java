package com.company.xmlgen.masterdata.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Master Data module error codes.
 *
 * @see docs/06-api-design/p4_master-data-api.md
 */
public enum MasterDataTypeErrorCode implements ErrorCode {

    MASTER_DATA_TYPE_NOT_FOUND,
    MASTER_DATA_TYPE_CODE_ALREADY_EXISTS;

    @Override
    public String code() {
        return name();
    }
}
