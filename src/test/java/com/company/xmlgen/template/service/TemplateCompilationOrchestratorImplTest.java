package com.company.xmlgen.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.entity.MasterDataFieldDataType;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.template.domain.RuntimeTemplate;
import com.company.xmlgen.template.domain.TemplateCompileContext;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateMappingEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateFieldRepository;
import com.company.xmlgen.template.repository.TemplateMappingRepository;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemplateCompilationOrchestratorImplTest {

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
    private TemplateSchemaParser templateSchemaParser;

    @Mock
    private TemplateSchemaCompiler templateSchemaCompiler;

    private TemplateCompilationOrchestratorImpl orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new TemplateCompilationOrchestratorImpl(
                templateRepository,
                templateFieldRepository,
                templateMappingRepository,
                masterDataFieldRepository,
                masterDataTypeRepository,
                templateSchemaParser,
                templateSchemaCompiler);
    }

    @Test
    void compileAndPersist_withMetadata_persistsCompiledSchemaJson() {
        TemplateEntity template = new TemplateEntity("LIVE_GAME", "Live Game", TemplateStatus.ACTIVE, 1L);
        TemplateFieldEntity field = mock(TemplateFieldEntity.class);
        when(field.getId()).thenReturn(100L);
        when(field.getFieldName()).thenReturn("GameKindId");
        TemplateMappingEntity mapping = new TemplateMappingEntity(10L, 100L, 200L);
        MasterDataFieldEntity masterDataField = new MasterDataFieldEntity(
                300L, "game_kind_id", "Game Kind ID", MasterDataFieldDataType.INTEGER, true, 1);
        MasterDataTypeEntity masterDataType =
                new MasterDataTypeEntity("GAME_KIND", "Game Kind", MasterDataTypeStatus.ACTIVE);
        RuntimeTemplate runtimeTemplate = new RuntimeTemplate(List.of());
        ObjectNode compiled = new ObjectMapper().createObjectNode().put("compiled", true);

        when(templateRepository.findById(10L)).thenReturn(Optional.of(template));
        when(templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of(field));
        when(templateMappingRepository.findAllByTemplateId(10L)).thenReturn(List.of(mapping));
        when(masterDataFieldRepository.findById(200L)).thenReturn(Optional.of(masterDataField));
        when(masterDataTypeRepository.findById(300L)).thenReturn(Optional.of(masterDataType));
        when(templateSchemaParser.parse(template, List.of(field))).thenReturn(runtimeTemplate);
        when(templateSchemaCompiler.compile(eq(runtimeTemplate), any(TemplateCompileContext.class)))
                .thenReturn(compiled);

        orchestrator.compileAndPersist(10L);

        ArgumentCaptor<TemplateCompileContext> contextCaptor = ArgumentCaptor.forClass(TemplateCompileContext.class);
        verify(templateSchemaParser).parse(template, List.of(field));
        verify(templateSchemaCompiler).compile(eq(runtimeTemplate), contextCaptor.capture());
        assertThat(contextCaptor.getValue().mappings()).hasSize(1);
        assertThat(contextCaptor.getValue().mappings().get(0).fieldName()).isEqualTo("GameKindId");
        assertThat(contextCaptor.getValue().mappings().get(0).masterDataTypeCode()).isEqualTo("GAME_KIND");
        assertThat(contextCaptor.getValue().mappings().get(0).masterDataFieldName()).isEqualTo("game_kind_id");
        verify(templateRepository).save(template);
        assertThat(template.getCompiledSchemaJson()).isEqualTo(compiled);
    }

    @Test
    void compileAndPersist_withoutMetadata_clearsCompiledSchemaJson() {
        TemplateEntity template = new TemplateEntity("LIVE_GAME", "Live Game", TemplateStatus.ACTIVE, 1L);
        template.setCompiledSchemaJson(new ObjectMapper().createObjectNode().put("legacy", true));

        when(templateRepository.findById(10L)).thenReturn(Optional.of(template));
        when(templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of());

        orchestrator.compileAndPersist(10L);

        verify(templateSchemaParser, never()).parse(any(), any());
        verify(templateSchemaCompiler, never()).compile(any(), any());
        verify(templateRepository).save(template);
        assertThat(template.getCompiledSchemaJson()).isNull();
    }

    @Test
    void compileAndPersist_templateNotFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orchestrator.compileAndPersist(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);
    }

    @Test
    void compileAndPersist_repeatedCompilation_usesSameCompilerOutput() {
        TemplateEntity template = new TemplateEntity("LIVE_GAME", "Live Game", TemplateStatus.ACTIVE, 1L);
        TemplateFieldEntity field = new TemplateFieldEntity(
                10L, "Game", "Game", TemplateFieldNodeType.ELEMENT, TemplateFieldEmptyHandling.REQUIRED, 1);
        RuntimeTemplate runtimeTemplate = new RuntimeTemplate(List.of());
        JsonNode compiled = new ObjectMapper().createObjectNode().put("version", 1);

        when(templateRepository.findById(10L)).thenReturn(Optional.of(template));
        when(templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of(field));
        when(templateMappingRepository.findAllByTemplateId(10L)).thenReturn(List.of());
        when(templateSchemaParser.parse(template, List.of(field))).thenReturn(runtimeTemplate);
        when(templateSchemaCompiler.compile(eq(runtimeTemplate), any(TemplateCompileContext.class)))
                .thenReturn(compiled);

        orchestrator.compileAndPersist(10L);
        orchestrator.compileAndPersist(10L);

        verify(templateSchemaCompiler, org.mockito.Mockito.times(2))
                .compile(eq(runtimeTemplate), any(TemplateCompileContext.class));
        assertThat(template.getCompiledSchemaJson()).isEqualTo(compiled);
    }
}
