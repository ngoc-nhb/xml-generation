package com.company.xmlgen.xmlgeneration.dto;

import java.util.List;

/**
 * Optional debug view of a resolved execution node for Preview responses.
 */
public record PreviewExecutionNodeResponse(
        String fieldName,
        String xmlName,
        String value,
        List<PreviewExecutionNodeResponse> children) {

    public PreviewExecutionNodeResponse {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
