package com.company.xmlgen.xmlgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.repository.TemplateFieldRepository;
import com.company.xmlgen.template.repository.TemplateMappingRepository;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.template.service.RuntimeLoaderImpl;
import com.company.xmlgen.template.service.TemplateCompileMappingResolver;
import com.company.xmlgen.template.service.TemplateCompileMappingResolverImpl;
import com.company.xmlgen.xmlgeneration.dto.ExportRequest;
import com.company.xmlgen.xmlgeneration.dto.ExportResponse;
import com.company.xmlgen.xmlgeneration.exception.ExportErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Long TEMPLATE_ID = 10L;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateFieldRepository templateFieldRepository;

    @Mock
    private TemplateMappingRepository templateMappingRepository;

    @Mock
    private MasterDataFieldRepository masterDataFieldRepository;

    @Mock
    private MasterDataTypeRepository masterDataTypeRepository;

    @Mock
    private TemplateCompileMappingResolver templateCompileMappingResolver;

    @Mock
    private RuntimeExecutionOrchestrator runtimeExecutionOrchestrator;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportServiceImpl(
                templateRepository, templateCompileMappingResolver, runtimeExecutionOrchestrator);
    }

    @Test
    void export_success_returnsExportResponse() throws Exception {
        TemplateEntity template = compiledTemplate(validCompiledSchema());
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(templateCompileMappingResolver.resolveByTemplateId(TEMPLATE_ID)).thenReturn(List.of());
        when(runtimeExecutionOrchestrator.execute(any())).thenReturn(RuntimeExecutionResult.success("<Game/>", null));

        ExportResponse response = exportService.export(new ExportRequest(
                TEMPLATE_ID,
                OBJECT_MAPPER.readTree("""
                        { "GameId": 123 }
                        """),
                null));

        assertThat(response.successful()).isTrue();
        assertThat(response.xml()).isEqualTo("<Game/>");
        assertThat(response.validationErrors()).isEmpty();
    }

    @Test
    void export_validationFailure_returnsExportResponseWithErrors() throws Exception {
        TemplateEntity template = compiledTemplate(validCompiledSchema());
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(templateCompileMappingResolver.resolveByTemplateId(TEMPLATE_ID)).thenReturn(List.of());
        when(runtimeExecutionOrchestrator.execute(any()))
                .thenReturn(RuntimeExecutionResult.validationFailed(RuntimeValidationResult.invalid(List.of(
                        new RuntimeValidationError("GameId", "SOURCE_TYPE_REQUIRED", "sourceType is required")))));

        ExportResponse response = exportService.export(new ExportRequest(TEMPLATE_ID, OBJECT_MAPPER.createObjectNode(), null));

        assertThat(response.successful()).isFalse();
        assertThat(response.xml()).isNull();
        assertThat(response.validationErrors()).hasSize(1);
        assertThat(response.validationErrors().getFirst().code()).isEqualTo("SOURCE_TYPE_REQUIRED");
    }

    @Test
    void export_missingTemplate_throwsNotFoundException() {
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.export(new ExportRequest(TEMPLATE_ID, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void export_missingCompiledSchema_throwsBusinessException() {
        TemplateEntity template = new TemplateEntity("CODE", "Name", TemplateStatus.ACTIVE, 1L);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> exportService.export(new ExportRequest(TEMPLATE_ID, null, null)))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode())
                        .isEqualTo(ExportErrorCode.TEMPLATE_NOT_COMPILED));

        verify(runtimeExecutionOrchestrator, never()).execute(any());
    }

    @Test
    void export_emptyInput_buildsExecutionRequestWithEmptyObject() throws Exception {
        TemplateEntity template = compiledTemplate(validCompiledSchema());
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(templateCompileMappingResolver.resolveByTemplateId(TEMPLATE_ID)).thenReturn(List.of());
        when(runtimeExecutionOrchestrator.execute(any())).thenReturn(RuntimeExecutionResult.success("<Game/>", null));

        exportService.export(new ExportRequest(TEMPLATE_ID, null, null));

        ArgumentCaptor<RuntimeExecutionRequest> captor = ArgumentCaptor.forClass(RuntimeExecutionRequest.class);
        verify(runtimeExecutionOrchestrator).execute(captor.capture());
        assertThat(captor.getValue().inputData().isObject()).isTrue();
        assertThat(captor.getValue().inputData().isEmpty()).isTrue();
    }

    @Test
    void export_repeatedExport_isDeterministic() throws Exception {
        TemplateCompileMappingResolver mappingResolver = new TemplateCompileMappingResolverImpl(
                templateFieldRepository,
                templateMappingRepository,
                masterDataFieldRepository,
                masterDataTypeRepository);
        ExportService integrationExportService = new ExportServiceImpl(
                templateRepository,
                mappingResolver,
                new RuntimeExecutionOrchestratorImpl(
                        new RuntimeLoaderImpl(),
                        new RuntimeValidationServiceImpl(List.of(
                                new HierarchyValidationRule(),
                                new NodeTypeValidationRule(),
                                new OccurrenceValidationRule(),
                                new EmptyHandlingValidationRule())),
                        new ValueResolutionServiceImpl(),
                        new XMLGenerationServiceImpl()));

        TemplateEntity template = compiledTemplate(validCompiledSchema());
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(TEMPLATE_ID)).thenReturn(List.of());
        when(templateMappingRepository.findAllByTemplateId(TEMPLATE_ID)).thenReturn(List.of());

        ExportRequest request = new ExportRequest(
                TEMPLATE_ID,
                OBJECT_MAPPER.readTree("""
                        { "GameId": 123 }
                        """),
                null);

        ExportResponse first = integrationExportService.export(request);
        ExportResponse second = integrationExportService.export(request);

        assertThat(first.successful()).isTrue();
        assertThat(first.xml()).isEqualTo(second.xml());
    }

    @Test
    void export_repeatableGroup_returnsMultipleGoalInfoElements() throws Exception {
        TemplateCompileMappingResolver mappingResolver = new TemplateCompileMappingResolverImpl(
                templateFieldRepository,
                templateMappingRepository,
                masterDataFieldRepository,
                masterDataTypeRepository);
        ExportService integrationExportService = new ExportServiceImpl(
                templateRepository,
                mappingResolver,
                new RuntimeExecutionOrchestratorImpl(
                        new RuntimeLoaderImpl(),
                        new RuntimeValidationServiceImpl(List.of(
                                new HierarchyValidationRule(),
                                new NodeTypeValidationRule(),
                                new OccurrenceValidationRule(),
                                new EmptyHandlingValidationRule())),
                        new ValueResolutionServiceImpl(),
                        new XMLGenerationServiceImpl()));

        TemplateEntity template = compiledTemplate(repeatableCompiledSchema());
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(TEMPLATE_ID)).thenReturn(List.of());
        when(templateMappingRepository.findAllByTemplateId(TEMPLATE_ID)).thenReturn(List.of());

        ExportResponse response = integrationExportService.export(new ExportRequest(
                TEMPLATE_ID,
                OBJECT_MAPPER.readTree("""
                        {
                          "GoalInfo": [
                            { "Time": 17 },
                            { "Time": 35 }
                          ]
                        }
                        """),
                null));

        assertThat(response.successful()).isTrue();
        assertThat(response.xml()).contains("<GoalInfo><Time>17</Time></GoalInfo>");
        assertThat(response.xml()).contains("<GoalInfo><Time>35</Time></GoalInfo>");
    }

    @Test
    void export_buildsExecutionRequestWithCompiledSchemaAndMappings() throws Exception {
        TemplateEntity template = compiledTemplate(validCompiledSchema());
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(templateCompileMappingResolver.resolveByTemplateId(TEMPLATE_ID)).thenReturn(List.of());
        when(runtimeExecutionOrchestrator.execute(any())).thenReturn(RuntimeExecutionResult.success("<Game/>", null));

        exportService.export(new ExportRequest(TEMPLATE_ID, OBJECT_MAPPER.createObjectNode(), null));

        ArgumentCaptor<RuntimeExecutionRequest> captor = ArgumentCaptor.forClass(RuntimeExecutionRequest.class);
        verify(runtimeExecutionOrchestrator).execute(captor.capture());
        assertThat(captor.getValue().compiledSchemaJson()).isEqualTo(template.getCompiledSchemaJson());
        assertThat(captor.getValue().mappings()).isEmpty();
    }

    private static TemplateEntity compiledTemplate(com.fasterxml.jackson.databind.JsonNode compiledSchemaJson) {
        TemplateEntity template = new TemplateEntity("CODE", "Name", TemplateStatus.ACTIVE, 1L);
        template.setCompiledSchemaJson(compiledSchemaJson);
        return template;
    }

    private static com.fasterxml.jackson.databind.JsonNode validCompiledSchema() throws Exception {
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

    private static com.fasterxml.jackson.databind.JsonNode repeatableCompiledSchema() throws Exception {
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
                          "fieldName": "GoalInfo",
                          "name": "GoalInfo",
                          "fieldType": "GROUP",
                          "occurrenceRule": "ZERO_OR_MORE",
                          "emptyHandling": "REQUIRED",
                          "requiredWhenParentExists": false,
                          "displayOrder": 1,
                          "children": [
                            {
                              "fieldName": "Time",
                              "name": "Time",
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
                      ]
                    }
                  ],
                  "mappings": []
                }
                """);
    }
}
