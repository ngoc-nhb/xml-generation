package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.xmlgeneration.domain.ExecutionPlan;

/**
 * Converts a runtime template hierarchy into an immutable execution plan.
 */
public interface RuntimeExecutionPlanner {

    ExecutionPlan plan(RuntimeTemplate runtimeTemplate);
}
