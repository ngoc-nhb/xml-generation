package com.company.xmlgen.xmlgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionNode;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;
import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.xmlgeneration.exception.RuntimeValidationErrorCode;
import com.company.xmlgen.xmlgeneration.exception.RuntimeValidationException;
import com.fasterxml.jackson.databind.node.NullNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResolvedValueValidationServiceImplTest {

    private ResolvedValueValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ResolvedValueValidationServiceImpl();
    }

    @Test
    void validate_requiredFieldWithEmptyValue_reportsRequiredFieldMissing() {
        RuntimeExecutionTree tree = tree(element("RequiredField", TemplateFieldEmptyHandling.REQUIRED, NullNode.instance));

        RuntimeValidationResult result = validationService.validate(tree);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors())
                .extracting(RuntimeValidationError::code)
                .containsExactly(ResolvedValueValidationServiceImpl.REQUIRED_FIELD_MISSING);
        assertThat(result.errors().getFirst().fieldName()).isEqualTo("RequiredField");
    }

    @Test
    void validate_zeroIfEmptyWithNullValue_isValid() {
        RuntimeExecutionTree tree = tree(element("ZeroField", TemplateFieldEmptyHandling.ZERO_IF_EMPTY, NullNode.instance));

        RuntimeValidationResult result = validationService.validate(tree);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validate_omitIfEmptyWithNullValue_isValid() {
        RuntimeExecutionTree tree = tree(element("OmitField", TemplateFieldEmptyHandling.OMIT_IF_EMPTY, NullNode.instance));

        RuntimeValidationResult result = validationService.validate(tree);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validate_emptyTagIfEmptyWithNullValue_isValid() {
        RuntimeExecutionTree tree =
                tree(element("EmptyTagField", TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY, NullNode.instance));

        RuntimeValidationResult result = validationService.validate(tree);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validate_nullExecutionTree_throwsValidationException() {
        assertThatThrownBy(() -> validationService.validate(null))
                .isInstanceOfSatisfying(RuntimeValidationException.class, ex -> assertThat(ex.getValidationErrorCode())
                        .isEqualTo(RuntimeValidationErrorCode.EXECUTION_TREE_REQUIRED));
    }

    private static RuntimeExecutionTree tree(RuntimeExecutionNode child) {
        RuntimeExecutionNode root = new RuntimeExecutionNode(
                group("Game", "Game"),
                NullNode.instance,
                List.of(child));
        return new RuntimeExecutionTree(List.of(root));
    }

    private static RuntimeField group(String fieldName, String xmlName) {
        return new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                TemplateFieldNodeType.GROUP,
                null,
                null,
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
                List.of());
    }

    private static RuntimeExecutionNode element(
            String fieldName, TemplateFieldEmptyHandling emptyHandling, com.fasterxml.jackson.databind.JsonNode value) {
        return new RuntimeExecutionNode(
                new RuntimeField(
                        fieldName,
                        fieldName,
                        fieldName,
                        TemplateFieldNodeType.ELEMENT,
                        null,
                        null,
                        null,
                        emptyHandling,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1,
                        null,
                        List.of()),
                value,
                List.of());
    }
}
