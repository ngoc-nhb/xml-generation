package com.company.xmlgen.template.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileContext;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Compiles runtime Template schema into the persisted compiled schema JSON.
 */
public interface TemplateSchemaCompiler {

    JsonNode compile(RuntimeTemplate runtimeTemplate, TemplateCompileContext compileContext);
}
