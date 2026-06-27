package com.company.xmlgen.masterdata.entity;

/**
 * Data type of a {@link MasterDataFieldEntity}.
 *
 * @see docs/03-database-design/03-database-design.md §4.6
 */
public enum MasterDataFieldDataType {

    STRING,
    INTEGER,
    LONG,
    DECIMAL,
    BOOLEAN,
    DATE,
    DATETIME
}
