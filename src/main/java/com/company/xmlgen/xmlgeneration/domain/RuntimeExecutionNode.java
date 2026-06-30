package com.company.xmlgen.xmlgeneration.domain;

import com.company.xmlgen.template.domain.RuntimeField;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * One node in a {@link RuntimeExecutionTree} with resolved execution value.
 *
 * <p>This is an execution artifact node, not a canonical runtime model.
 */
public record RuntimeExecutionNode(RuntimeField field, JsonNode value, List<RuntimeExecutionNode> children) {

    public RuntimeExecutionNode {
        children = List.copyOf(children);
    }
}
