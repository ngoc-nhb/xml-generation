package com.company.xmlgen.masterdata.service;

import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 * Default Master Data Record validator.
 */
@Service
public class MasterDataValidationServiceImpl implements MasterDataValidationService {

    private final MasterDataFieldRepository masterDataFieldRepository;
    private final List<ValidationRule> validationRules;

    public MasterDataValidationServiceImpl(
            MasterDataFieldRepository masterDataFieldRepository,
            List<ValidationRule> validationRules) {
        this.masterDataFieldRepository = masterDataFieldRepository;
        this.validationRules = validationRules.stream()
                .sorted(Comparator.comparingInt(ValidationRule::priority))
                .toList();
    }

    @Override
    public ValidationResult validate(ValidationContext context) {
        List<MasterDataFieldEntity> fields =
                masterDataFieldRepository.findAllByMasterDataTypeId(context.typeId());
        ValidationContext enrichedContext = context.withFields(fields);

        List<ValidationError> errors = validationRules.stream()
                .flatMap(rule -> toStream(rule.validate(enrichedContext)))
                .toList();

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        }
        return ValidationResult.invalid(errors);
    }

    private static Stream<ValidationError> toStream(List<ValidationError> errors) {
        return errors == null ? Stream.empty() : errors.stream();
    }
}
