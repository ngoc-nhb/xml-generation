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
import com.company.xmlgen.xmlgeneration.exception.ValueResolutionErrorCode;
import com.company.xmlgen.xmlgeneration.exception.ValueResolutionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValueResolutionServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ValueResolutionService valueResolutionService;

    @BeforeEach
    void setUp() {
        valueResolutionService = new ValueResolutionServiceImpl();
    }

    @Test
    void resolve_inputValue_readsFromInputScope() throws Exception {
        RuntimeTemplate runtime = runtime(group(
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
                        1)));
        ValueResolutionContext context = context(json("""
                { "GameDate": "20260618" }
                """));

        RuntimeExecutionTree resolved = valueResolutionService.resolve(runtime, context);

        assertThat(resolved.roots()).hasSize(1);
        assertThat(valueOf(resolved.roots().getFirst().children().getFirst())).isEqualTo("20260618");
    }

    @Test
    void resolve_staticValue_readsFromRuntimeField() throws Exception {
        RuntimeTemplate runtime = runtime(group(
                "Game",
                "Game",
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1,
                element(
                        "Version",
                        "Version",
                        TemplateFieldSourceType.STATIC,
                        TemplateFieldValueType.STRING,
                        null,
                        1,
                        "1")));
        ValueResolutionContext context = context(json("{}"));

        RuntimeExecutionNode version = valueResolutionService
                .resolve(runtime, context)
                .roots()
                .getFirst()
                .children()
                .getFirst();

        assertThat(valueOf(version)).isEqualTo("1");
    }

    @Test
    void resolve_defaultValueFallback_appliesWhenInputMissingOrEmpty() throws Exception {
        RuntimeTemplate missingInput = runtime(group(
                "Game",
                "Game",
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1,
                element(
                        "Weather",
                        "Weather",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.STRING,
                        "Sunny",
                        1)));
        RuntimeTemplate emptyInput = runtime(group(
                "Game",
                "Game",
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1,
                element(
                        "Weather",
                        "Weather",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.STRING,
                        "Sunny",
                        1)));

        assertThat(valueOf(valueResolutionService
                        .resolve(missingInput, context(json("{}")))
                        .roots()
                        .getFirst()
                        .children()
                        .getFirst()))
                .isEqualTo("Sunny");
        assertThat(valueOf(valueResolutionService
                        .resolve(emptyInput, context(json("""
                                { "Weather": "" }
                                """)))
                        .roots()
                        .getFirst()
                        .children()
                        .getFirst()))
                .isEqualTo("Sunny");
    }

    @Test
    void resolve_masterDataValue_usesMappingMetadataFromContext() throws Exception {
        RuntimeTemplate runtime = runtime(group(
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
                        1)));
        ValueResolutionContext context = new ValueResolutionContext(
                json("{}"),
                json("""
                        {
                          "GAME_KIND": {
                            "game_kind_id": 2
                          }
                        }
                        """),
                List.of(new TemplateCompileMapping("GameKindId", "GAME_KIND", "game_kind_id")));

        RuntimeExecutionNode resolved = valueResolutionService
                .resolve(runtime, context)
                .roots()
                .getFirst()
                .children()
                .getFirst();

        assertThat(resolved.value().asInt()).isEqualTo(2);
    }

    @Test
    void resolve_nestedHierarchy_scopesInputToGroupOccurrence() throws Exception {
        RuntimeTemplate runtime = runtime(group(
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
                                1)),
                element(
                        "GameId",
                        "GameID",
                        TemplateFieldSourceType.INPUT,
                        TemplateFieldValueType.INTEGER,
                        null,
                        2)));
        ValueResolutionContext context = context(json("""
                {
                  "GameId": 123,
                  "GoalInfo": {
                    "Time": 17
                  }
                }
                """));

        RuntimeExecutionNode game = valueResolutionService.resolve(runtime, context).roots().getFirst();

        assertThat(valueOf(game.children().get(1))).isEqualTo("123");
        assertThat(game.children().get(0).children().getFirst().value().asInt()).isEqualTo(17);
    }

    @Test
    void resolve_repeatableGroup_producesOneRuntimeExecutionNodePerOccurrence() throws Exception {
        RuntimeTemplate runtime = runtime(group(
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
                                1),
                        element(
                                "PlayerId",
                                "PlayerID",
                                TemplateFieldSourceType.MASTER_DATA,
                                TemplateFieldValueType.INTEGER,
                                null,
                                2))));
        ValueResolutionContext context = new ValueResolutionContext(
                json("""
                        {
                          "GoalInfo": [
                            { "Time": 17 },
                            { "Time": 35 }
                          ]
                        }
                        """),
                json("""
                        {
                          "GoalInfo": [
                            { "PLAYER": { "player_id": 1001 } },
                            { "PLAYER": { "player_id": 1002 } }
                          ]
                        }
                        """),
                List.of(new TemplateCompileMapping("PlayerId", "PLAYER", "player_id")));

        List<RuntimeExecutionNode> goalInfoOccurrences = valueResolutionService
                .resolve(runtime, context)
                .roots()
                .getFirst()
                .children();

        assertThat(goalInfoOccurrences).hasSize(2);
        assertThat(goalInfoOccurrences.get(0).children().get(0).value().asInt()).isEqualTo(17);
        assertThat(goalInfoOccurrences.get(0).children().get(1).value().asInt()).isEqualTo(1001);
        assertThat(goalInfoOccurrences.get(1).children().get(0).value().asInt()).isEqualTo(35);
        assertThat(goalInfoOccurrences.get(1).children().get(1).value().asInt()).isEqualTo(1002);
    }

    @Test
    void resolve_repeatedResolution_isDeterministic() throws Exception {
        RuntimeTemplate runtime = runtime(group(
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
                        1)));
        ValueResolutionContext context = context(json("""
                { "GameDate": "20260618" }
                """));

        RuntimeExecutionTree first = valueResolutionService.resolve(runtime, context);
        RuntimeExecutionTree second = valueResolutionService.resolve(runtime, context);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void resolve_doesNotModifyRuntimeTemplate() throws Exception {
        RuntimeTemplate runtime = runtime(group(
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
                        1)));
        RuntimeTemplate before = copyRuntime(runtime);
        ValueResolutionContext context = context(json("""
                { "GameDate": "20260618" }
                """));

        valueResolutionService.resolve(runtime, context);

        assertThat(runtime).isEqualTo(before);
    }

    @Test
    void resolve_nullRuntimeTemplate_throwsResolutionException() {
        assertThatThrownBy(() -> valueResolutionService.resolve(null, context(json("{}"))))
                .isInstanceOfSatisfying(ValueResolutionException.class, ex -> assertThat(ex.getResolutionErrorCode())
                        .isEqualTo(ValueResolutionErrorCode.RUNTIME_TEMPLATE_REQUIRED));
    }

    @Test
    void resolve_nullContext_throwsResolutionException() {
        RuntimeTemplate runtime = runtime(group(
                "Game",
                "Game",
                TemplateFieldOccurrenceRule.ONE_OR_MORE,
                1));

        assertThatThrownBy(() -> valueResolutionService.resolve(runtime, null))
                .isInstanceOfSatisfying(ValueResolutionException.class, ex -> assertThat(ex.getResolutionErrorCode())
                        .isEqualTo(ValueResolutionErrorCode.RESOLUTION_CONTEXT_REQUIRED));
    }

    private static RuntimeTemplate runtime(RuntimeField... roots) {
        return new RuntimeTemplate(List.of(roots));
    }

    private static RuntimeTemplate copyRuntime(RuntimeTemplate runtime) {
        return new RuntimeTemplate(runtime.roots().stream().map(ValueResolutionServiceImplTest::copyField).toList());
    }

    private static RuntimeField copyField(RuntimeField field) {
        return new RuntimeField(
                field.fieldName(),
                field.xmlName(),
                field.displayName(),
                field.nodeType(),
                field.valueType(),
                field.sourceType(),
                field.occurrenceRule(),
                field.emptyHandling(),
                field.requiredWhenParentExists(),
                field.triggerActivation(),
                field.defaultValue(),
                field.staticValue(),
                field.xmlPath(),
                field.namespace(),
                field.displayOrder(),
                field.description(),
                field.children().stream().map(ValueResolutionServiceImplTest::copyField).toList());
    }

    private static ValueResolutionContext context(JsonNode inputData) {
        return new ValueResolutionContext(inputData, null, List.of());
    }

    private static JsonNode json(String content) throws Exception {
        return OBJECT_MAPPER.readTree(content);
    }

    private static String valueOf(RuntimeExecutionNode field) {
        return field.value().asText();
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
        return element(fieldName, xmlName, sourceType, valueType, defaultValue, displayOrder, null);
    }

    private static RuntimeField element(
            String fieldName,
            String xmlName,
            TemplateFieldSourceType sourceType,
            TemplateFieldValueType valueType,
            String defaultValue,
            int displayOrder,
            String staticValue) {
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
                staticValue,
                null,
                null,
                displayOrder,
                null,
                List.of());
    }
}
