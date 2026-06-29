package com.company.xmlgen.template.entity;

/**
 * Occurrence rule of a {@link TemplateFieldEntity}.
 *
 * @see docs/03-database-design/03-database-design.md §4.3
 */
public enum TemplateFieldOccurrenceRule {

    ONE_OR_MORE,
    ZERO_OR_MORE,
    ZERO_OR_ONE
}
