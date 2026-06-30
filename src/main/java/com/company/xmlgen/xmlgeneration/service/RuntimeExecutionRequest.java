package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Transport object for runtime pipeline execution inputs.
 *
 * <p>Immutable data carrier only. Do not add helper methods or business logic (for example
 * {@code loadMappings()}, {@code validate()}, or {@code compile()}). Extend with new immutable
 * fields when additional execution inputs are required.
 */
public record RuntimeExecutionRequest(
        JsonNode compiledSchemaJson,
        JsonNode inputData,
        JsonNode selectedMasterData,
        List<TemplateCompileMapping> mappings) {

    public RuntimeExecutionRequest {
        mappings = mappings == null ? List.of() : List.copyOf(mappings);
    }
}
