package com.company.xmlgen.template.service;

/**
 * Coordinates Template schema compilation and persistence of {@code compiled_schema_json}.
 */
public interface TemplateCompilationOrchestrator {

    /**
     * Loads editable metadata, compiles it, and persists the generated schema JSON.
     *
     * <p>When no metadata exists, clears {@code compiled_schema_json}.
     */
    void compileAndPersist(Long templateId);
}
