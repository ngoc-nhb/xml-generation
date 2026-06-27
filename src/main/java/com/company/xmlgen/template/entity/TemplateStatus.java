package com.company.xmlgen.template.entity;

/**
 * Lifecycle status of a {@link TemplateEntity}.
 *
 * @see docs/03-database-design/03-database-design.md §4.2
 */
public enum TemplateStatus {

    ACTIVE,
    INACTIVE;

    public static TemplateStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return valueOf(value.trim().toUpperCase());
    }
}
