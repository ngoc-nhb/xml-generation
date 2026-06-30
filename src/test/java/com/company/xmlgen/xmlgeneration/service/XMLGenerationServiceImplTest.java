package com.company.xmlgen.xmlgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.template.domain.RuntimeField;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionNode;
import com.company.xmlgen.xmlgeneration.domain.RuntimeExecutionTree;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationErrorCode;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XMLGenerationServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ValueResolutionService valueResolutionService;
    private XMLGenerationService xmlGenerationService;

    @BeforeEach
    void setUp() {
        valueResolutionService = new ValueResolutionServiceImpl();
        xmlGenerationService = new XMLGenerationServiceImpl();
    }

    @Test
    void generate_simpleXml_writesDeclarationAndElement() throws Exception {
        RuntimeExecutionTree tree = resolveTree(
                group(
                        "Game",
                        "Game",
                        TemplateFieldOccurrenceRule.ONE_OR_MORE,
                        1,
                        element(
                                "GameId",
                                "GameID",
                                TemplateFieldSourceType.INPUT,
                                TemplateFieldValueType.INTEGER,
                                null,
                                1)),
                """
                { "GameId": 123 }
                """);

        String xml = xmlGenerationService.generate(tree);

        assertThat(xml).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertThat(xml).contains("<Game>");
        assertThat(xml).contains("<GameID>123</GameID>");
        assertThat(xml).contains("</Game>");
    }

    @Test
    void generate_nestedHierarchy_writesNestedElements() throws Exception {
        RuntimeExecutionTree tree = resolveTree(
                group(
                        "Game",
                        "Game",
                        TemplateFieldOccurrenceRule.ONE_OR_MORE,
                        1,
                        group(
                                "GoalInfo",
                                "GoalInfo",
                                TemplateFieldOccurrenceRule.ZERO_OR_ONE,
                                1,
                                element(
                                        "Time",
                                        "Time",
                                        TemplateFieldSourceType.INPUT,
                                        TemplateFieldValueType.INTEGER,
                                        null,
                                        1))),
                """
                { "GoalInfo": { "Time": 17 } }
                """);

        String xml = xmlGenerationService.generate(tree);

        assertThat(xml).contains("<GoalInfo>");
        assertThat(xml).contains("<Time>17</Time>");
        assertThat(xml).contains("</GoalInfo>");
    }

    @Test
    void generate_attributes_writesAttributeOnParentElement() {
        RuntimeExecutionTree tree = tree(group(
                "Player",
                "Player",
                TemplateFieldNodeType.GROUP,
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                TemplateFieldEmptyHandling.REQUIRED,
                1,
                attribute("PlayerId", "ID", "1001", 1)));

        String xml = xmlGenerationService.generate(tree);

        assertThat(xml).contains("<Player ID=\"1001\">");
        assertThat(xml).contains("</Player>");
    }

    @Test
    void generate_repeatableGroups_writesMultipleSiblingElements() throws Exception {
        RuntimeExecutionTree tree = resolveTree(
                group(
                        "Game",
                        "Game",
                        TemplateFieldOccurrenceRule.ONE_OR_MORE,
                        1,
                        group(
                                "GoalInfo",
                                "GoalInfo",
                                TemplateFieldOccurrenceRule.ZERO_OR_MORE,
                                1,
                                element(
                                        "Time",
                                        "Time",
                                        TemplateFieldSourceType.INPUT,
                                        TemplateFieldValueType.INTEGER,
                                        null,
                                        1))),
                """
                {
                  "GoalInfo": [
                    { "Time": 17 },
                    { "Time": 35 }
                  ]
                }
                """);

        String xml = xmlGenerationService.generate(tree);

        assertThat(xml).contains("<GoalInfo><Time>17</Time></GoalInfo>");
        assertThat(xml).contains("<GoalInfo><Time>35</Time></GoalInfo>");
    }

    @Test
    void generate_emptyElements_appliesEmptyHandlingRules() {
        RuntimeExecutionTree tree = tree(group(
                "Game",
                "Game",
                TemplateFieldNodeType.GROUP,
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                TemplateFieldEmptyHandling.REQUIRED,
                1,
                element(
                        "OmitMe",
                        "OmitMe",
                        TemplateFieldEmptyHandling.OMIT_IF_EMPTY,
                        NullNode.instance,
                        1),
                element(
                        "EmptyTag",
                        "EmptyTag",
                        TemplateFieldEmptyHandling.EMPTY_TAG_IF_EMPTY,
                        NullNode.instance,
                        2),
                element(
                        "ZeroValue",
                        "ZeroValue",
                        TemplateFieldEmptyHandling.ZERO_IF_EMPTY,
                        NullNode.instance,
                        3)));

        String xml = xmlGenerationService.generate(tree);

        assertThat(xml).doesNotContain("OmitMe");
        assertThat(xml).contains("<EmptyTag></EmptyTag>");
        assertThat(xml).contains("<ZeroValue>0</ZeroValue>");
    }

    @Test
    void generate_escapesSpecialCharacters() {
        RuntimeExecutionTree tree = tree(group(
                "Game",
                "Game",
                TemplateFieldNodeType.GROUP,
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                TemplateFieldEmptyHandling.REQUIRED,
                1,
                element(
                        "Name",
                        "Name",
                        TemplateFieldEmptyHandling.REQUIRED,
                        TextNode.valueOf("A & B <C>"),
                        1)));

        String xml = xmlGenerationService.generate(tree);

        assertThat(xml).contains("<Name>A &amp; B &lt;C&gt;</Name>");
    }

    @Test
    void generate_repeatedGeneration_isDeterministic() throws Exception {
        RuntimeExecutionTree tree = resolveTree(
                group(
                        "Game",
                        "Game",
                        TemplateFieldOccurrenceRule.ONE_OR_MORE,
                        1,
                        element(
                                "GameDate",
                                "GameDate",
                                TemplateFieldSourceType.INPUT,
                                TemplateFieldValueType.STRING,
                                null,
                                1)),
                """
                { "GameDate": "20260618" }
                """);

        String first = xmlGenerationService.generate(tree);
        String second = xmlGenerationService.generate(tree);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void generate_valueResolutionPipeline_producesExpectedXml() throws Exception {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(group(
                "Game",
                "Game",
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1,
                element(
                        "GameKindId",
                        "GameKindID",
                        TemplateFieldSourceType.MASTER_DATA,
                        TemplateFieldValueType.INTEGER,
                        null,
                        1),
                element(
                        "Title",
                        "Title",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.STRING,
                        null,
                        2))));
        ValueResolutionContext context = new ValueResolutionContext(
                OBJECT_MAPPER.readTree("""
                        { "Title": "Match" }
                        """),
                OBJECT_MAPPER.readTree("""
                        { "GAME_KIND": { "game_kind_id": 2 } }
                        """),
                List.of(new TemplateCompileMapping("GameKindId", "GAME_KIND", "game_kind_id")));
        RuntimeExecutionTree tree = valueResolutionService.resolve(runtime, context);

        String xml = xmlGenerationService.generate(tree);

        assertThat(xml).contains("<GameKindID>2</GameKindID>");
        assertThat(xml).contains("<Title>Match</Title>");
    }

    @Test
    void generate_nullExecutionTree_throwsGenerationException() {
        assertThatThrownBy(() -> xmlGenerationService.generate(null))
                .isInstanceOfSatisfying(XMLGenerationException.class, ex -> assertThat(ex.getGenerationErrorCode())
                        .isEqualTo(XMLGenerationErrorCode.EXECUTION_TREE_REQUIRED));
    }

    @Test
    void generate_multipleRoots_throwsGenerationException() {
        RuntimeExecutionTree tree = new RuntimeExecutionTree(List.of(
                node("First", "First", TemplateFieldNodeType.GROUP, NullNode.instance, List.of()),
                node("Second", "Second", TemplateFieldNodeType.GROUP, NullNode.instance, List.of())));

        assertThatThrownBy(() -> xmlGenerationService.generate(tree))
                .isInstanceOfSatisfying(XMLGenerationException.class, ex -> assertThat(ex.getGenerationErrorCode())
                        .isEqualTo(XMLGenerationErrorCode.INVALID_EXECUTION_TREE));
    }

    private RuntimeExecutionTree resolveTree(RuntimeField root, String inputJson) throws Exception {
        RuntimeTemplate runtime = new RuntimeTemplate(List.of(root));
        ValueResolutionContext context = new ValueResolutionContext(OBJECT_MAPPER.readTree(inputJson), null, List.of());
        return valueResolutionService.resolve(runtime, context);
    }

    private static RuntimeExecutionTree tree(RuntimeExecutionNode root) {
        return new RuntimeExecutionTree(List.of(root));
    }

    private static RuntimeExecutionNode node(
            String fieldName,
            String xmlName,
            TemplateFieldNodeType nodeType,
            com.fasterxml.jackson.databind.JsonNode value,
            List<RuntimeExecutionNode> children) {
        RuntimeField field = new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                nodeType,
                nodeType == TemplateFieldNodeType.ATTRIBUTE ? TemplateFieldValueType.STRING : null,
                null,
                nodeType == TemplateFieldNodeType.GROUP ? TemplateFieldOccurrenceRule.ONE_OR_MORE : null,
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
        return new RuntimeExecutionNode(field, value, children);
    }

    private static RuntimeExecutionNode group(
            String fieldName,
            String xmlName,
            TemplateFieldNodeType nodeType,
            TemplateFieldOccurrenceRule occurrenceRule,
            TemplateFieldEmptyHandling emptyHandling,
            int displayOrder,
            RuntimeExecutionNode... children) {
        List<RuntimeField> childFields =
                java.util.Arrays.stream(children).map(RuntimeExecutionNode::field).toList();
        RuntimeField field = new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                nodeType,
                null,
                null,
                occurrenceRule,
                emptyHandling,
                false,
                null,
                null,
                null,
                null,
                null,
                displayOrder,
                null,
                childFields);
        return new RuntimeExecutionNode(field, NullNode.instance, List.of(children));
    }

    private static RuntimeExecutionNode element(
            String fieldName,
            String xmlName,
            TemplateFieldEmptyHandling emptyHandling,
            com.fasterxml.jackson.databind.JsonNode value,
            int displayOrder) {
        RuntimeField field = new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                TemplateFieldNodeType.ELEMENT,
                TemplateFieldValueType.STRING,
                null,
                null,
                emptyHandling,
                false,
                null,
                null,
                null,
                null,
                null,
                displayOrder,
                null,
                List.of());
        return new RuntimeExecutionNode(field, value, List.of());
    }

    private static RuntimeExecutionNode attribute(String fieldName, String xmlName, String value, int displayOrder) {
        RuntimeField field = new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                TemplateFieldNodeType.ATTRIBUTE,
                TemplateFieldValueType.STRING,
                TemplateFieldSourceType.STATIC,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                false,
                null,
                null,
                value,
                null,
                null,
                displayOrder,
                null,
                List.of());
        return new RuntimeExecutionNode(field, TextNode.valueOf(value), List.of());
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
            String defaultValue,
            int displayOrder) {
        return new RuntimeField(
                fieldName,
                xmlName,
                fieldName,
                TemplateFieldNodeType.ELEMENT,
                valueType,
                sourceType,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                false,
                null,
                defaultValue,
                null,
                null,
                null,
                displayOrder,
                null,
                List.of());
    }
}
