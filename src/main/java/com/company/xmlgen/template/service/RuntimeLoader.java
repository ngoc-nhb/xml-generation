package com.company.xmlgen.template.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Reconstructs a runtime template hierarchy from persisted compiled schema JSON.
 */
public interface RuntimeLoader {

    /**
     * Loads {@code compiled_schema_json} into a {@link RuntimeTemplate}.
     *
     * @param compiledSchemaJson compiled schema produced by {@link TemplateSchemaCompiler}
     */
    RuntimeTemplate load(JsonNode compiledSchemaJson);
}
