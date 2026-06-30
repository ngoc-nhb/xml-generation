package com.company.xmlgen.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileContext;
import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.RuntimeLoaderErrorCode;
import com.company.xmlgen.template.exception.RuntimeLoaderException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuntimeLoaderImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TemplateSchemaParser parser = new TemplateSchemaParserImpl();
    private final TemplateSchemaCompiler compiler = new TemplateSchemaCompilerImpl(objectMapper);
    private final RuntimeLoader loader = new RuntimeLoaderImpl();

    @Test
    void load_simpleHierarchy_rebuildsRuntimeTree() {
        JsonNode compiled = compiledJson(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "GROUP",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "GameId",
                          "name": "GameID",
                          "fieldType": "ELEMENT",
                          "sourceType": "INPUT",
                          "emptyHandling": "REQUIRED",
                          "requiredWhenParentExists": false,
                          "displayOrder": 1,
                          "children": []
                        }
                      ]
                    }
                  ],
                  "mappings": []
                }
                """);

        RuntimeTemplate runtime = loader.load(compiled);

        assertThat(runtime.roots()).hasSize(1);
        assertThat(runtime.roots().getFirst().fieldName()).isEqualTo("Game");
        assertThat(runtime.roots().getFirst().children()).hasSize(1);
        assertThat(runtime.roots().getFirst().children().getFirst().fieldName()).isEqualTo("GameId");
    }

    @Test
    void load_nestedGroups_rebuildsDeepHierarchy() {
        JsonNode compiled = compiledJson(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "GROUP",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "GoalInfo",
                          "name": "GoalInfo",
                          "fieldType": "GROUP",
                          "emptyHandling": "REQUIRED",
                          "requiredWhenParentExists": false,
                          "displayOrder": 1,
                          "children": [
                            {
                              "fieldName": "PlayerId",
                              "name": "PlayerID",
                              "fieldType": "ELEMENT",
                              "sourceType": "INPUT",
                              "emptyHandling": "REQUIRED",
                              "requiredWhenParentExists": false,
                              "displayOrder": 1,
                              "children": []
                            }
                          ]
                        }
                      ]
                    }
                  ],
                  "mappings": []
                }
                """);

        RuntimeTemplate runtime = loader.load(compiled);

        assertThat(runtime.roots().getFirst().children().getFirst().fieldName()).isEqualTo("GoalInfo");
        assertThat(runtime.roots().getFirst().children().getFirst().children().getFirst().fieldName())
                .isEqualTo("PlayerId");
    }

    @Test
    void load_ignoresMappingMetadataInCompiledJson() {
        JsonNode compiled = compiledJson(
                """
                {
                  "roots": [
                    {
                      "fieldName": "GameKindId",
                      "name": "GameKindID",
                      "fieldType": "ELEMENT",
                      "sourceType": "MASTER_DATA",
                      "masterDataType": "GAME_KIND",
                      "masterDataField": "game_kind_id",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": []
                    }
                  ],
                  "mappings": [
                    {
                      "fieldName": "GameKindId",
                      "masterDataType": "GAME_KIND",
                      "masterDataField": "game_kind_id"
                    }
                  ]
                }
                """);

        RuntimeTemplate runtime = loader.load(compiled);

        assertThat(runtime.roots()).hasSize(1);
        assertThat(runtime.roots().getFirst().sourceType()).isEqualTo(TemplateFieldSourceType.MASTER_DATA);
        assertThat(compiled.get("mappings")).hasSize(1);
        assertThat(compiled.at("/mappings/0/fieldName").asText()).isEqualTo("GameKindId");
    }

    @Test
    void load_repeatedLoad_isDeterministic() {
        JsonNode compiled = compiledJson(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Second",
                      "name": "Second",
                      "fieldType": "ELEMENT",
                      "sourceType": "INPUT",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 2,
                      "children": []
                    },
                    {
                      "fieldName": "First",
                      "name": "First",
                      "fieldType": "ELEMENT",
                      "sourceType": "INPUT",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": []
                    }
                  ],
                  "mappings": []
                }
                """);

        RuntimeTemplate first = loader.load(compiled);
        RuntimeTemplate second = loader.load(compiled);

        assertThat(first).isEqualTo(second);
        assertThat(first.roots()).extracting("fieldName").containsExactly("First", "Second");
    }

    @Test
    void load_parserCompilerRoundTrip_producesStructurallyEquivalentRuntimeTemplate() {
        TemplateEntity template = template();
        TemplateFieldEntity root = field(1L, null, "Game", "Game", TemplateFieldNodeType.GROUP, null, 1);
        TemplateFieldEntity child = field(
                2L, 1L, "GameKindId", "GameKindID", TemplateFieldNodeType.ELEMENT, TemplateFieldSourceType.MASTER_DATA, 1);
        when(child.getValueType()).thenReturn(TemplateFieldValueType.INTEGER);
        when(child.getDisplayName()).thenReturn("Game Kind ID");

        RuntimeTemplate parsed = parser.parse(template, List.of(child, root));
        TemplateCompileContext context = new TemplateCompileContext(List.of(
                new TemplateCompileMapping("GameKindId", "GAME_KIND", "game_kind_id")));
        JsonNode compiled = compiler.compile(parsed, context);

        RuntimeTemplate loaded = loader.load(compiled);

        assertThat(loaded).isEqualTo(parsed);
        assertThat(compiled.get("mappings")).hasSize(1);
    }

    @Test
    void load_nestedParserCompilerRoundTrip_producesStructurallyEquivalentRuntimeTemplate() {
        TemplateEntity template = template();
        TemplateFieldEntity root = field(1L, null, "Game", "Game", TemplateFieldNodeType.GROUP, null, 1);
        TemplateFieldEntity child = field(
                2L, 1L, "Title", "Title", TemplateFieldNodeType.ELEMENT, TemplateFieldSourceType.INPUT, 1);
        TemplateFieldEntity grandchild = field(
                3L, 2L, "Subtitle", "Subtitle", TemplateFieldNodeType.ELEMENT, TemplateFieldSourceType.STATIC, 2);
        when(grandchild.getStaticValue()).thenReturn("fixed");

        RuntimeTemplate parsed = parser.parse(template, List.of(grandchild, child, root));
        JsonNode compiled = compiler.compile(parsed, TemplateCompileContext.empty());
        RuntimeTemplate loaded = loader.load(compiled);

        assertThat(loaded).isEqualTo(parsed);
    }

    @Test
    void load_containerSavedAsElementWithoutSourceType_normalizesToGroup() {
        JsonNode compiled = compiledJson(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Football",
                      "name": "Football",
                      "fieldType": "ELEMENT",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "GameID",
                          "name": "GameID",
                          "fieldType": "ELEMENT",
                          "sourceType": "INPUT",
                          "dataType": "STRING",
                          "emptyHandling": "REQUIRED",
                          "requiredWhenParentExists": false,
                          "displayOrder": 1,
                          "children": []
                        }
                      ]
                    }
                  ],
                  "mappings": []
                }
                """);

        RuntimeTemplate runtime = loader.load(compiled);

        assertThat(runtime.roots().getFirst().fieldName()).isEqualTo("Football");
        assertThat(runtime.roots().getFirst().nodeType()).isEqualTo(TemplateFieldNodeType.GROUP);
        assertThat(runtime.roots().getFirst().sourceType()).isNull();
    }

    @Test
    void load_nullCompiledSchema_throwsLoaderException() {
        assertThatThrownBy(() -> loader.load(null))
                .isInstanceOfSatisfying(RuntimeLoaderException.class, ex -> assertThat(ex.getLoaderErrorCode())
                        .isEqualTo(RuntimeLoaderErrorCode.COMPILED_SCHEMA_INVALID));
    }

    @Test
    void load_missingRoots_throwsLoaderException() {
        ObjectNode compiled = objectMapper.createObjectNode();
        compiled.putArray("mappings");

        assertThatThrownBy(() -> loader.load(compiled))
                .isInstanceOfSatisfying(RuntimeLoaderException.class, ex -> assertThat(ex.getLoaderErrorCode())
                        .isEqualTo(RuntimeLoaderErrorCode.COMPILED_SCHEMA_INVALID));
    }

    @Test
    void load_invalidFieldType_throwsLoaderException() {
        JsonNode compiled = compiledJson(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "INVALID",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": []
                    }
                  ],
                  "mappings": []
                }
                """);

        assertThatThrownBy(() -> loader.load(compiled))
                .isInstanceOfSatisfying(RuntimeLoaderException.class, ex -> assertThat(ex.getLoaderErrorCode())
                        .isEqualTo(RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID));
    }

    private JsonNode compiledJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static TemplateEntity template() {
        return new TemplateEntity("LIVE_GAME", "Live Game", TemplateStatus.ACTIVE, 1L);
    }

    private static TemplateFieldEntity field(
            Long id,
            Long parentId,
            String fieldName,
            String xmlName,
            TemplateFieldNodeType nodeType,
            TemplateFieldSourceType sourceType,
            int displayOrder) {
        TemplateFieldEntity field = mock(TemplateFieldEntity.class);
        when(field.getId()).thenReturn(id);
        when(field.getParentId()).thenReturn(parentId);
        when(field.getFieldName()).thenReturn(fieldName);
        when(field.getXmlName()).thenReturn(xmlName);
        when(field.getDisplayName()).thenReturn(fieldName);
        when(field.getNodeType()).thenReturn(nodeType);
        when(field.getValueType()).thenReturn(null);
        when(field.getSourceType()).thenReturn(sourceType);
        when(field.getOccurrenceRule()).thenReturn(null);
        when(field.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(field.isRequiredWhenParentExists()).thenReturn(false);
        when(field.getTriggerActivation()).thenReturn(null);
        when(field.getDefaultValue()).thenReturn(null);
        when(field.getStaticValue()).thenReturn(null);
        when(field.getXmlPath()).thenReturn(null);
        when(field.getNamespace()).thenReturn(null);
        when(field.getDisplayOrder()).thenReturn(displayOrder);
        when(field.getDescription()).thenReturn(null);
        return field;
    }
}
