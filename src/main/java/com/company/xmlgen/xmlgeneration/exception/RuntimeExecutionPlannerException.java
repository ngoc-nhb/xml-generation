package com.company.xmlgen.xmlgeneration.exception;

import com.company.xmlgen.exception.ApplicationException;

/**
 * Raised when a runtime template cannot be converted into an execution plan.
 */
public class RuntimeExecutionPlannerException extends ApplicationException {

    private final RuntimeExecutionPlannerErrorCode plannerErrorCode;

    public RuntimeExecutionPlannerException(RuntimeExecutionPlannerErrorCode errorCode, String message) {
        super(errorCode, message);
        this.plannerErrorCode = errorCode;
    }

    public RuntimeExecutionPlannerErrorCode getPlannerErrorCode() {
        return plannerErrorCode;
    }
}
