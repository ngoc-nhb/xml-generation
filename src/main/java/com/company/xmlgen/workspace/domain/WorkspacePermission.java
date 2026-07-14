package com.company.xmlgen.workspace.domain;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Capability codes stored on {@code workspace_members.permissions}.
 *
 * <p>Add new values here when introducing capabilities — no schema change required.
 */
public enum WorkspacePermission {
    IMPORT_TEMPLATE,
    MANAGE_MASTER_DATA;

    public static Set<String> allCodes() {
        return Arrays.stream(values())
                .map(Enum::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static WorkspacePermission fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Permission code is required");
        }
        return valueOf(code.trim().toUpperCase(Locale.ROOT));
    }

    public static Set<String> normalizeCodes(Iterable<String> codes) {
        Set<String> normalized = new LinkedHashSet<>();
        if (codes == null) {
            return normalized;
        }
        for (String code : codes) {
            normalized.add(fromCode(code).name());
        }
        return normalized;
    }
}
