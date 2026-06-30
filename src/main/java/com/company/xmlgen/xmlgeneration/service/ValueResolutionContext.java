package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Runtime execution inputs for value resolution.
 */
public record ValueResolutionContext(
        JsonNode inputData, JsonNode selectedMasterData, List<TemplateCompileMapping> mappings) {

    public ValueResolutionContext {
        mappings = mappings == null ? List.of() : List.copyOf(mappings);
    }

    public Map<String, TemplateCompileMapping> mappingsByFieldName() {
        return mappings.stream()
                .collect(Collectors.toMap(
                        TemplateCompileMapping::fieldName, Function.identity(), (first, ignored) -> first));
    }
}
