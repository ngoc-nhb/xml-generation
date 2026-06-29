package com.company.xmlgen.template.entity;

/**
 * Value type of a {@link TemplateFieldEntity}.
 *
 * @see docs/03-database-design/03-database-design.md §4.3
 */
public enum TemplateFieldValueType {

    STRING,
    INTEGER,
    LONG,
    DECIMAL,
    BOOLEAN,
    DATE,
    DATETIME
}
