package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;

/**
 * Serializes a {@link com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree} into XML.
 *
 * <p>MVP exposes {@link #generate(RuntimeExecutionTree)} returning {@link String}. Future phases
 * may add {@code Writer} and {@code OutputStream} overloads without changing the traversal
 * algorithm — only the output sink changes.
 */
public interface XMLGenerationService {

    String generate(RuntimeExecutionTree executionTree);
}
