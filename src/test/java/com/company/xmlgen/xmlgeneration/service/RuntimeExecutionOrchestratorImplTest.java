package com.company.xmlgen.xmlgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.template.domain.TemplateCompileMapping;
import com.company.xmlgen.template.exception.RuntimeLoaderErrorCode;
import com.company.xmlgen.template.exception.RuntimeLoaderException;
import com.company.xmlgen.template.service.RuntimeLoaderImpl;
import com.company.xmlgen.xmlgeneration.exception.ValueResolutionErrorCode;
import com.company.xmlgen.xmlgeneration.exception.ValueResolutionException;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationErrorCode;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuntimeExecutionOrchestratorImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RuntimeExecutionOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new RuntimeExecutionOrchestratorImpl(
                new RuntimeLoaderImpl(),
                new RuntimeValidationServiceImpl(List.of(
                        new HierarchyValidationRule(),
                        new NodeTypeValidationRule(),
                        new OccurrenceValidationRule())),
                new ValueResolutionServiceImpl(),
                new ResolvedValueValidationServiceImpl(),
                new XMLGenerationServiceImpl());
    }

    @Test
    void execute_successfulPipeline_returnsXmlAndExecutionTree() throws Exception {
        RuntimeExecutionRequest request = request(
                validCompiledSchema(),
                """
                { "GameId": 123 }
                """,
                "{}",
                List.of());

        RuntimeExecutionResult result = orchestrator.execute(request);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.validationResult().isValid()).isTrue();
        assertThat(result.executionTree()).isNotNull();
        assertThat(result.xml()).contains("<GameID>123</GameID>");
        assertThat(result.xml()).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    @Test
    void execute_validationFailure_stopsBeforeLaterStages() throws Exception {
        ValueResolutionService valueResolutionService = mock(ValueResolutionService.class);
        XMLGenerationService xmlGenerationService = mock(XMLGenerationService.class);
        RuntimeExecutionOrchestrator failingOrchestrator = new RuntimeExecutionOrchestratorImpl(
                new RuntimeLoaderImpl(),
                new RuntimeValidationServiceImpl(List.of(
                        new HierarchyValidationRule(),
                        new NodeTypeValidationRule(),
                        new OccurrenceValidationRule())),
                valueResolutionService,
                new ResolvedValueValidationServiceImpl(),
                xmlGenerationService);

        RuntimeExecutionResult result = failingOrchestrator.execute(request(
                invalidCompiledSchemaMissingSourceType(),
                """
                { "GameId": 123 }
                """,
                "{}",
                List.of()));

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.validationResult().isValid()).isFalse();
        assertThat(result.xml()).isNull();
        assertThat(result.executionTree()).isNull();
        verify(valueResolutionService, never()).resolve(any(), any());
        verify(xmlGenerationService, never()).generate(any());
    }

    @Test
    void execute_valueResolutionFailure_propagatesException() throws Exception {
        ValueResolutionService valueResolutionService = mock(ValueResolutionService.class);
        when(valueResolutionService.resolve(any(), any()))
                .thenThrow(new ValueResolutionException(
                        ValueResolutionErrorCode.RESOLUTION_CONTEXT_REQUIRED, "ValueResolutionContext is required"));
        RuntimeExecutionOrchestrator failingOrchestrator = new RuntimeExecutionOrchestratorImpl(
                new RuntimeLoaderImpl(),
                new RuntimeValidationServiceImpl(List.of(
                        new HierarchyValidationRule(),
                        new NodeTypeValidationRule(),
                        new OccurrenceValidationRule())),
                valueResolutionService,
                new ResolvedValueValidationServiceImpl(),
                new XMLGenerationServiceImpl());

        assertThatThrownBy(() -> failingOrchestrator.execute(request(
                        validCompiledSchema(),
                        """
                        { "GameId": 123 }
                        """,
                        "{}",
                        List.of())))
                .isInstanceOfSatisfying(ValueResolutionException.class, ex -> assertThat(ex.getResolutionErrorCode())
                        .isEqualTo(ValueResolutionErrorCode.RESOLUTION_CONTEXT_REQUIRED));
    }

    @Test
    void execute_xmlGenerationSuccess_isIncludedInSuccessfulResult() throws Exception {
        RuntimeExecutionResult result = orchestrator.execute(request(
                validCompiledSchema(),
                """
                { "GameId": 456 }
                """,
                "{}",
                List.of()));

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.xml()).contains("<GameID>456</GameID>");
    }

    @Test
    void execute_repeatedExecution_isDeterministic() throws Exception {
        RuntimeExecutionRequest request = request(
                validCompiledSchema(),
                """
                { "GameId": 789 }
                """,
                "{}",
                List.of());

        RuntimeExecutionResult first = orchestrator.execute(request);
        RuntimeExecutionResult second = orchestrator.execute(request);

        assertThat(first.xml()).isEqualTo(second.xml());
        assertThat(first.executionTree()).isEqualTo(second.executionTree());
    }

    @Test
    void execute_nullCompiledSchema_propagatesLoaderException() {
        RuntimeExecutionRequest request =
                new RuntimeExecutionRequest(null, OBJECT_MAPPER.createObjectNode(), null, List.of());

        assertThatThrownBy(() -> orchestrator.execute(request))
                .isInstanceOfSatisfying(RuntimeLoaderException.class, ex -> assertThat(ex.getLoaderErrorCode())
                        .isEqualTo(RuntimeLoaderErrorCode.COMPILED_SCHEMA_INVALID));
    }

    @Test
    void execute_emptyCompiledSchemaRoots_propagatesXmlGenerationException() throws Exception {
        ObjectNode compiled = OBJECT_MAPPER.createObjectNode();
        compiled.putArray("roots");
        compiled.putArray("mappings");

        RuntimeExecutionRequest request = new RuntimeExecutionRequest(compiled, OBJECT_MAPPER.createObjectNode(), null, List.of());

        assertThatThrownBy(() -> orchestrator.execute(request))
                .isInstanceOfSatisfying(XMLGenerationException.class, ex -> assertThat(ex.getGenerationErrorCode())
                        .isEqualTo(XMLGenerationErrorCode.INVALID_EXECUTION_TREE));
    }

    @Test
    void execute_invalidCompiledSchema_propagatesLoaderException() throws Exception {
        RuntimeExecutionRequest request = request(
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
                """,
                "{}",
                "{}",
                List.of());

        assertThatThrownBy(() -> orchestrator.execute(request))
                .isInstanceOfSatisfying(RuntimeLoaderException.class, ex -> assertThat(ex.getLoaderErrorCode())
                        .isEqualTo(RuntimeLoaderErrorCode.COMPILED_SCHEMA_FIELD_INVALID));
    }

    @Test
    void execute_blankOptionalFields_appliesEmptyHandlingWithoutValidationErrors() throws Exception {
        RuntimeExecutionResult result = orchestrator.execute(request(
                compiledSchemaWithEmptyHandlingFields(),
                "{}",
                "{}",
                List.of()));

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.xml()).doesNotContain("OmitField");
        assertThat(result.xml()).contains("<EmptyTagField></EmptyTagField>");
        assertThat(result.xml()).contains("<ZeroField>0</ZeroField>");
    }

    @Test
    void execute_blankRequiredField_reportsRequiredFieldMissing() throws Exception {
        RuntimeExecutionResult result = orchestrator.execute(request(
                compiledSchemaWithRequiredField(),
                "{}",
                "{}",
                List.of()));

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.validationResult().errors())
                .extracting(RuntimeValidationError::code)
                .containsExactly(ResolvedValueValidationServiceImpl.REQUIRED_FIELD_MISSING);
        assertThat(result.validationResult().errors().getFirst().fieldName()).isEqualTo("RequiredField");
    }

    @Test
    void execute_stringFieldWithZeroIfEmpty_succeedsWithoutInvalidEmptyHandling() throws Exception {
        RuntimeExecutionResult result = orchestrator.execute(request(
                compiledSchemaWithStringZeroIfEmptyField(),
                "{}",
                "{}",
                List.of()));

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.xml()).contains("<PlayHistoryNo>0</PlayHistoryNo>");
    }

    @Test
    void execute_masterDataMapping_resolvesThroughPipeline() throws Exception {
        RuntimeExecutionResult result = orchestrator.execute(request(
                validCompiledSchemaWithMasterDataField(),
                """
                { "Title": "Match" }
                """,
                """
                { "GAME_KIND": { "game_kind_id": 2 } }
                """,
                List.of(new TemplateCompileMapping("GameKindId", "GAME_KIND", "game_kind_id"))));

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.xml()).contains("<GameKindID>2</GameKindID>");
        assertThat(result.xml()).contains("<Title>Match</Title>");
    }

    private static JsonNode compiledSchemaWithEmptyHandlingFields() throws Exception {
        return OBJECT_MAPPER.readTree(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "GROUP",
                      "occurrenceRule": "ONE_OR_MORE",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "OmitField",
                          "name": "OmitField",
                          "fieldType": "ELEMENT",
                          "sourceType": "INPUT",
                          "dataType": "STRING",
                          "emptyHandling": "OMIT_IF_EMPTY",
                          "requiredWhenParentExists": false,
                          "displayOrder": 1,
                          "children": []
                        },
                        {
                          "fieldName": "EmptyTagField",
                          "name": "EmptyTagField",
                          "fieldType": "ELEMENT",
                          "sourceType": "INPUT",
                          "dataType": "STRING",
                          "emptyHandling": "EMPTY_TAG_IF_EMPTY",
                          "requiredWhenParentExists": false,
                          "displayOrder": 2,
                          "children": []
                        },
                        {
                          "fieldName": "ZeroField",
                          "name": "ZeroField",
                          "fieldType": "ELEMENT",
                          "sourceType": "INPUT",
                          "dataType": "INTEGER",
                          "emptyHandling": "ZERO_IF_EMPTY",
                          "requiredWhenParentExists": false,
                          "displayOrder": 3,
                          "children": []
                        }
                      ]
                    }
                  ],
                  "mappings": []
                }
                """);
    }

    private static JsonNode compiledSchemaWithRequiredField() throws Exception {
        return OBJECT_MAPPER.readTree(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "GROUP",
                      "occurrenceRule": "ONE_OR_MORE",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "RequiredField",
                          "name": "RequiredField",
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
    }

    private static JsonNode compiledSchemaWithStringZeroIfEmptyField() throws Exception {
        return OBJECT_MAPPER.readTree(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "GROUP",
                      "occurrenceRule": "ONE_OR_MORE",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "PlayHistoryNo",
                          "name": "PlayHistoryNo",
                          "fieldType": "ELEMENT",
                          "sourceType": "INPUT",
                          "dataType": "STRING",
                          "emptyHandling": "ZERO_IF_EMPTY",
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
    }

    private static RuntimeExecutionRequest request(
            JsonNode compiledSchemaJson,
            String inputJson,
            String masterDataJson,
            List<TemplateCompileMapping> mappings) throws Exception {
        return new RuntimeExecutionRequest(
                compiledSchemaJson,
                OBJECT_MAPPER.readTree(inputJson),
                OBJECT_MAPPER.readTree(masterDataJson),
                mappings);
    }

    private static RuntimeExecutionRequest request(
            String compiledSchemaJson,
            String inputJson,
            String masterDataJson,
            List<TemplateCompileMapping> mappings) throws Exception {
        return request(OBJECT_MAPPER.readTree(compiledSchemaJson), inputJson, masterDataJson, mappings);
    }

    private static JsonNode validCompiledSchema() throws Exception {
        return OBJECT_MAPPER.readTree(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "GROUP",
                      "occurrenceRule": "ONE_OR_MORE",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "GameId",
                          "name": "GameID",
                          "fieldType": "ELEMENT",
                          "sourceType": "INPUT",
                          "dataType": "INTEGER",
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
    }

    private static JsonNode validCompiledSchemaWithMasterDataField() throws Exception {
        return OBJECT_MAPPER.readTree(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "GROUP",
                      "occurrenceRule": "ONE_OR_MORE",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "GameKindId",
                          "name": "GameKindID",
                          "fieldType": "ELEMENT",
                          "sourceType": "MASTER_DATA",
                          "dataType": "INTEGER",
                          "emptyHandling": "REQUIRED",
                          "requiredWhenParentExists": false,
                          "displayOrder": 1,
                          "children": []
                        },
                        {
                          "fieldName": "Title",
                          "name": "Title",
                          "fieldType": "ELEMENT",
                          "sourceType": "INPUT",
                          "dataType": "STRING",
                          "emptyHandling": "REQUIRED",
                          "requiredWhenParentExists": false,
                          "displayOrder": 2,
                          "children": []
                        }
                      ]
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
    }

    private static JsonNode invalidCompiledSchemaMissingSourceType() throws Exception {
        return OBJECT_MAPPER.readTree(
                """
                {
                  "roots": [
                    {
                      "fieldName": "Game",
                      "name": "Game",
                      "fieldType": "GROUP",
                      "occurrenceRule": "ONE_OR_MORE",
                      "emptyHandling": "REQUIRED",
                      "requiredWhenParentExists": false,
                      "displayOrder": 1,
                      "children": [
                        {
                          "fieldName": "GameId",
                          "name": "GameID",
                          "fieldType": "ELEMENT",
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
    }
}
