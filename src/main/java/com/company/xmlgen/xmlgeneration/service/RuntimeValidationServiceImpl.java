package com.company.xmlgen.xmlgeneration.service;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.xmlgeneration.exception.RuntimeValidationErrorCode;
import com.company.xmlgen.xmlgeneration.exception.RuntimeValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Default runtime template validator.
 */
@Service
public class RuntimeValidationServiceImpl implements RuntimeValidationService {

    private final List<RuntimeValidationRule> validationRules;

    public RuntimeValidationServiceImpl(List<RuntimeValidationRule> validationRules) {
        this.validationRules = validationRules.stream()
                .sorted(Comparator.comparingInt(RuntimeValidationRule::priority))
                .toList();
    }

    @Override
    public RuntimeValidationResult validate(RuntimeTemplate runtimeTemplate) {
        if (runtimeTemplate == null) {
            throw new RuntimeValidationException(
                    RuntimeValidationErrorCode.RUNTIME_TEMPLATE_REQUIRED, "RuntimeTemplate is required");
        }

        RuntimeValidationContext context = new RuntimeValidationContext(runtimeTemplate);
        List<RuntimeValidationError> errors = validationRules.stream()
                .flatMap(rule -> toStream(rule.validate(context)))
                .toList();

        if (errors.isEmpty()) {
            return RuntimeValidationResult.valid();
        }
        return RuntimeValidationResult.invalid(errors);
    }

    private static Stream<RuntimeValidationError> toStream(List<RuntimeValidationError> errors) {
        return errors == null ? Stream.empty() : errors.stream();
    }
}
