package com.company.xmlgen.xmlgeneration.domain;

import java.util.List;

/**
 * Immutable execution artifact produced by value resolution.
 *
 * <p>Materializes runtime occurrences and resolved values for downstream execution.
 * This is not a canonical runtime model; {@code RuntimeTemplate} remains the runtime
 * definition.
 */
public record RuntimeExecutionTree(List<RuntimeExecutionNode> roots) {

    public RuntimeExecutionTree {
        roots = List.copyOf(roots);
    }
}
