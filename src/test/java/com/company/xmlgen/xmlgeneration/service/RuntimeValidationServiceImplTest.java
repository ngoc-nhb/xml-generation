package com.company.xmlgen.xmlgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import com.company.xmlgen.xmlgeneration.exception.RuntimeValidationErrorCode;
import com.company.xmlgen.xmlgeneration.exception.RuntimeValidationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuntimeValidationServiceImplTest {

    private RuntimeValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new RuntimeValidationServiceImpl(List.of(
                new HierarchyValidationRule(),
                new NodeTypeValidationRule(),
                new OccurrenceValidationRule(),
                new EmptyHandlingValidationRule()));
    }

    @Test
    void validate_validRuntimeTemplate_returnsValidResult() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(group(
                "Game",
                "Game",
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1,
                element(
                        "GameId",
                        "GameID",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.INTEGER,
                        TemplateFieldEmptyHandling.REQUIRED,
                        1))));

        RuntimeValidationResult result = validationService.validate(runtime);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void validate_invalidHierarchy_accumulatesHierarchyErrors() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(
                group("Game", "Game", TemplateFieldOccurrenceRule.ONE_OR_MORE, 1, element(
                        "GameId",
                        "GameID",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.INTEGER,
                        TemplateFieldEmptyHandling.REQUIRED,
                        1)),
                group("Game", "GameDuplicate", TemplateFieldOccurrenceRule.ONE_OR_MORE, 2)));

        RuntimeValidationResult result = validationService.validate(runtime);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors())
                .extracting(RuntimeValidationError::code)
                .contains(HierarchyValidationRule.DUPLICATE_FIELD_NAME);
    }

    @Test
    void validate_duplicateSiblingDisplayOrder_reportsHierarchyError() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(group(
                "Game",
                "Game",
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1,
                element(
                        "GameId",
                        "GameID",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.INTEGER,
                        TemplateFieldEmptyHandling.REQUIRED,
                        1),
                element(
                        "GameDate",
                        "GameDate",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.DATE,
                        TemplateFieldEmptyHandling.REQUIRED,
                        1))));

        RuntimeValidationResult result = validationService.validate(runtime);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors())
                .extracting(RuntimeValidationError::code)
                .contains(HierarchyValidationRule.DUPLICATE_DISPLAY_ORDER);
    }

    @Test
    void validate_invalidNodeType_reportsNodeTypeErrors() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(
                new RuntimeField(
                        "Game",
                        "Game",
                        "Game",
                        TemplateFieldNodeType.GROUP,
                        null,
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldOccurrenceRule.ONE_OR_MORE,
                        TemplateFieldEmptyHandling.REQUIRED,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1,
                        null,
                        List.of()),
                element(
                        "GameId",
                        "GameID",
                        null,
                        TemplateFieldValueType.INTEGER,
                        TemplateFieldEmptyHandling.REQUIRED,
                        1)));

        RuntimeValidationResult result = validationService.validate(runtime);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors())
                .extracting(RuntimeValidationError::code)
                .contains(
                        NodeTypeValidationRule.GROUP_SOURCE_TYPE_NOT_ALLOWED,
                        NodeTypeValidationRule.SOURCE_TYPE_REQUIRED);
    }

    @Test
    void validate_invalidOccurrenceConfiguration_reportsOccurrenceErrors() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(
                new RuntimeField(
                        "Game",
                        "Game",
                        "Game",
                        TemplateFieldNodeType.GROUP,
                        null,
                        null,
                        null,
                        TemplateFieldEmptyHandling.REQUIRED,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1,
                        null,
                        List.of(element(
                                "GameId",
                                "GameID",
                                TemplateFieldSourceType.INPUT,
                                TemplateFieldValueType.INTEGER,
                                TemplateFieldEmptyHandling.REQUIRED,
                                1,
                                TemplateFieldOccurrenceRule.ZERO_OR_ONE))),
                element(
                        "Title",
                        "Title",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.STRING,
                        TemplateFieldEmptyHandling.REQUIRED,
                        1,
                        TemplateFieldOccurrenceRule.ZERO_OR_MORE)));

        RuntimeValidationResult result = validationService.validate(runtime);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors())
                .extracting(RuntimeValidationError::code)
                .contains(
                        OccurrenceValidationRule.OCCURRENCE_RULE_REQUIRED,
                        OccurrenceValidationRule.OCCURRENCE_RULE_NOT_ALLOWED);
    }

    @Test
    void validate_invalidEmptyHandling_reportsEmptyHandlingError() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(group(
                "Game",
                "Game",
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1,
                element(
                        "GameDate",
                        "GameDate",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.DATE,
                        TemplateFieldEmptyHandling.ZERO_IF_EMPTY,
                        1))));

        RuntimeValidationResult result = validationService.validate(runtime);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors())
                .extracting(RuntimeValidationError::code)
                .contains(EmptyHandlingValidationRule.INVALID_EMPTY_HANDLING);
    }

    @Test
    void validate_multipleViolations_accumulatesErrorsFromAllRules() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(
                new RuntimeField(
                        "Game",
                        "Game",
                        "Game",
                        TemplateFieldNodeType.GROUP,
                        null,
                        TemplateFieldSourceType.STATIC,
                        null,
                        TemplateFieldEmptyHandling.REQUIRED,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1,
                        null,
                        List.of(element(
                                "GameDate",
                                "GameDate",
                                TemplateFieldSourceType.INPUT,
                                TemplateFieldValueType.DATE,
                                TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY,
                                1))),
                group("Game", "GameDuplicate", TemplateFieldOccurrenceRule.ONE_OR_MORE, 2)));

        RuntimeValidationResult result = validationService.validate(runtime);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors())
                .extracting(RuntimeValidationError::code)
                .contains(
                        HierarchyValidationRule.DUPLICATE_FIELD_NAME,
                        NodeTypeValidationRule.GROUP_SOURCE_TYPE_NOT_ALLOWED,
                        OccurrenceValidationRule.OCCURRENCE_RULE_REQUIRED,
                        EmptyHandlingValidationRule.INVALID_EMPTY_HANDLING);
        assertThat(result.errors().size()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void validate_nullRuntimeTemplate_throwsValidationException() {
        assertThatThrownBy(() -> validationService.validate(null))
                .isInstanceOfSatisfying(RuntimeValidationException.class, ex -> assertThat(ex.getValidationErrorCode())
                        .isEqualTo(RuntimeValidationErrorCode.RUNTIME_TEMPLATE_REQUIRED));
    }

    @Test
    void validate_exceedsMaxDepth_reportsDepthError() {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(nestedGroup("Level1", 21)));

        RuntimeValidationResult result = validationService.validate(runtime);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors())
                .extracting(RuntimeValidationError::code)
                .contains(HierarchyValidationRule.TEMPLATE_DEPTH_EXCEEDED);
    }

    private static RuntimeField nestedGroup(String fieldName, int remainingDepth) {
        if (remainingDepth <= 1) {
            return group(fieldName, fieldName, TemplateFieldOccurrenceRule.ONE_OR_MORE, 1);
        }
        return group(
                fieldName,
                fieldName,
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1,
                nestedGroup(fieldName + "Child", remainingDepth - 1));
    }

    private static RuntimeField group(
            String fieldName,
            String xmlName,
            TemplateFieldOccurrenceRule occurrenceRule,
            int displayOrder,
            RuntimeField... children) {
        return new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                TemplateFieldNodeType.GROUP,
                null,
                null,
                occurrenceRule,
                TemplateFieldEmptyHandling.REQUIRED,
                false,
                null,
                null,
                null,
                null,
                null,
                displayOrder,
                null,
                List.of(children));
    }

    private static RuntimeField element(
            String fieldName,
            String xmlName,
            TemplateFieldSourceType sourceType,
            TemplateFieldValueType valueType,
            TemplateFieldEmptyHandling emptyHandling,
            int displayOrder) {
        return element(fieldName, xmlName, sourceType, valueType, emptyHandling, displayOrder, null);
    }

    private static RuntimeField element(
            String fieldName,
            String xmlName,
            TemplateFieldSourceType sourceType,
            TemplateFieldValueType valueType,
            TemplateFieldEmptyHandling emptyHandling,
            int displayOrder,
            TemplateFieldOccurrenceRule occurrenceRule) {
        return new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                TemplateFieldNodeType.ELEMENT,
                valueType,
                sourceType,
                occurrenceRule,
                emptyHandling,
                false,
                null,
                null,
                sourceType == TemplateFieldSourceType.STATIC ? "static" : null,
                null,
                null,
                displayOrder,
                null,
                List.of());
    }
}
