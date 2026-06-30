package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationErrorCode;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationException;
import org.springframework.stereotype.Service;

/**
 * Default {@link XMLGenerationService} implementation.
 *
 * <p>Delegates serialization to the package-private {@link ExecutionTreeXmlWriter} helper.
 */
@Service
public class XMLGenerationServiceImpl implements XMLGenerationService {

    private final ExecutionTreeXmlWriter executionTreeXmlWriter;

    public XMLGenerationServiceImpl() {
        this(new ExecutionTreeXmlWriter());
    }

    XMLGenerationServiceImpl(ExecutionTreeXmlWriter executionTreeXmlWriter) {
        this.executionTreeXmlWriter = executionTreeXmlWriter;
    }

    @Override
    public String generate(RuntimeExecutionTree executionTree) {
        if (executionTree == null) {
            throw generationException(
                    XMLGenerationErrorCode.EXECUTION_TREE_REQUIRED, "RuntimeExecutionTree is required");
        }
        if (executionTree.roots().isEmpty()) {
            throw generationException(
                    XMLGenerationErrorCode.INVALID_EXECUTION_TREE, "RuntimeExecutionTree must contain a root node");
        }
        if (executionTree.roots().size() != 1) {
            throw generationException(
                    XMLGenerationErrorCode.INVALID_EXECUTION_TREE,
                    "RuntimeExecutionTree must contain exactly one root node");
        }

        try {
            return executionTreeXmlWriter.write(executionTree);
        } catch (XMLGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XMLGenerationException(
                    XMLGenerationErrorCode.INVALID_EXECUTION_TREE, "Failed to generate XML", ex);
        }
    }

    private static XMLGenerationException generationException(XMLGenerationErrorCode errorCode, String message) {
        return new XMLGenerationException(errorCode, message);
    }
}
