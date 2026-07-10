package com.company.xmlgen.authentication.domain;

/**
 * System-level role exposed through the user management API.
 */
public enum SystemRole {
    ADMIN,
    USER;

    public static SystemRole fromAdminFlag(boolean admin) {
        return admin ? ADMIN : USER;
    }

    public boolean toAdminFlag() {
        return this == ADMIN;
    }
}
