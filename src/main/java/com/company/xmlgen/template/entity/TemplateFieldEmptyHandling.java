package com.company.xmlgen.template.entity;

/**
 * Empty handling rule of a {@link TemplateFieldEntity}.
 *
 * @see docs/03-database-design/03-database-design.md §4.3
 */
public enum TemplateFieldEmptyHandling {

    REQUIRED,
    OMIT_IF_EMPTY,
    EMPTY_TAG_IF_EMPTY,
    ZERO_IF_EMPTY
}
