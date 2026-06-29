package com.company.xmlgen.template.domain;

import java.util.List;

/**
 * Additional compile-time inputs supplied by the caller.
 */
public record TemplateCompileContext(List<TemplateCompileMapping> mappings) {

    public TemplateCompileContext {
        mappings = mappings == null ? List.of() : List.copyOf(mappings);
    }

    public static TemplateCompileContext empty() {
        return new TemplateCompileContext(List.of());
    }
}
