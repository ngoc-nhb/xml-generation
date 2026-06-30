package com.company.xmlgen.xmlgeneration.domain;

import java.util.List;

/**
 * Immutable ordered execution model derived from a runtime template hierarchy.
 */
public record ExecutionPlan(List<ExecutionNode> roots) {

    public ExecutionPlan {
        roots = List.copyOf(roots);
    }
}
