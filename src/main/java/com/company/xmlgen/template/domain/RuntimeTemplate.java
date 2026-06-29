package com.company.xmlgen.template.domain;

import java.util.List;

/**
 * Runtime template schema hierarchy.
 */
public record RuntimeTemplate(List<RuntimeField> roots) {

    public RuntimeTemplate {
        roots = List.copyOf(roots);
    }
}
