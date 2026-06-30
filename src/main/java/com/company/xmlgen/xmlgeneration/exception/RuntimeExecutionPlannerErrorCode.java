package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ErrorCode;

/**
 * Error codes raised while building an execution plan from a runtime template.
 */
public enum RuntimeExecutionPlannerErrorCode implements ErrorCode {

    RUNTIME_TEMPLATE_REQUIRED;

    @Override
    public String code() {
        return name();
    }
}
