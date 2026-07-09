package com.company.xmlgen.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.ValidationException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.dto.request.CreateTemplateFieldRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateMappingRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateListResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateSchemaResponse;
import com.company.xmlgen.template.dto.response.UpdateTemplateResponse;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateMappingEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateFieldRepository;
import com.company.xmlgen.template.repository.TemplateMappingRepository;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TemplateServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String TEMPLATE_CODE = "LIVE_GAME";
    private static final String TEMPLATE_NAME = "Live Game";
    private static final String DESCRIPTION = "J League Live Match XML";

    private static final Long WORKSPACE_ID = 1L;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateFieldRepository templateFieldRepository;

    @Mock
    private TemplateMappingRepository templateMappingRepository;

    @Mock
    private MasterDataTypeRepository masterDataTypeRepository;

    @Mock
    private MasterDataFieldRepository masterDataFieldRepository;

    @Mock
    private MasterDataRecordRepository masterDataRecordRepository;

    @Mock
    private TemplateCompilationOrchestrator templateCompilationOrchestrator;

    private WorkspaceOwnershipGuard workspaceOwnershipGuard;
    private TemplateServiceImpl templateService;

    @BeforeEach
    void setUp() {
        workspaceOwnershipGuard = new WorkspaceOwnershipGuard(
                templateRepository,
                masterDataTypeRepository,
                masterDataFieldRepository,
                masterDataRecordRepository);
        templateService = new TemplateServiceImpl(
                templateRepository,
                templateFieldRepository,
                templateMappingRepository,
                templateCompilationOrchestrator,
                workspaceOwnershipGuard);
        WorkspaceTestSupport.useDefaultWorkspace();
        AuthenticatedUser currentUser = new AuthenticatedUser(USER_ID, "admin", true);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        WorkspaceTestSupport.clearWorkspace();
    }

    @Test
    void create_withoutSchema() {
        CreateTemplateRequest request = new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, null, null);
        when(templateRepository.existsByWorkspaceIdAndCode(WORKSPACE_ID, TEMPLATE_CODE)).thenReturn(false);
        TemplateEntity persisted = mock(TemplateEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(templateRepository.save(any(TemplateEntity.class))).thenReturn(persisted);

        CreateTemplateResponse response = templateService.create(request);

        assertThat(response.id()).isEqualTo(10L);

        ArgumentCaptor<TemplateEntity> captor = ArgumentCaptor.forClass(TemplateEntity.class);
        verify(templateRepository).save(captor.capture());
        TemplateEntity saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo(TEMPLATE_CODE);
        assertThat(saved.getName()).isEqualTo(TEMPLATE_NAME);
        assertThat(saved.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(saved.getStatus()).isEqualTo(TemplateStatus.ACTIVE);
        assertThat(saved.getCompiledSchemaJson()).isNull();
        assertThat(saved.getCreatedById()).isEqualTo(USER_ID);
        verify(templateCompilationOrchestrator, never()).compileAndPersist(any());
    }

    @Test
    void create_withSchema() {
        CreateTemplateFieldRequest rootField = new CreateTemplateFieldRequest(
                "Game",
                null,
                "Game",
                "Game",
                TemplateFieldNodeType.GROUP,
                null,
                null,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        CreateTemplateFieldRequest childField = new CreateTemplateFieldRequest(
                "GameId",
                "Game",
                "GameID",
                "Game ID",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        CreateTemplateSchemaRequest schema =
                new CreateTemplateSchemaRequest(List.of(rootField, childField), List.of());
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, schema, null);
        when(templateRepository.existsByWorkspaceIdAndCode(WORKSPACE_ID, TEMPLATE_CODE)).thenReturn(false);
        TemplateEntity persisted = mock(TemplateEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(templateRepository.save(any(TemplateEntity.class))).thenReturn(persisted);
        TemplateFieldEntity savedRoot = mock(TemplateFieldEntity.class);
        TemplateFieldEntity savedChild = mock(TemplateFieldEntity.class);
        when(savedRoot.getId()).thenReturn(100L);
        when(savedChild.getId()).thenReturn(101L);
        when(templateFieldRepository.save(any(TemplateFieldEntity.class)))
                .thenAnswer(invocation -> {
                    TemplateFieldEntity entity = invocation.getArgument(0);
                    if ("Game".equals(entity.getFieldName())) {
                        return savedRoot;
                    }
                    return savedChild;
                });

        CreateTemplateResponse response = templateService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        verify(templateFieldRepository, org.mockito.Mockito.times(2)).save(any(TemplateFieldEntity.class));
        verify(templateMappingRepository, never()).save(any(TemplateMappingEntity.class));
        verify(templateRepository).save(any(TemplateEntity.class));
        verify(templateCompilationOrchestrator).compileAndPersist(10L);
    }

    @Test
    void create_withSchemaAndMapping() {
        CreateTemplateFieldRequest field = new CreateTemplateFieldRequest(
                "GameKindId",
                null,
                "GameKindID",
                "Game Kind ID",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.MASTER_DATA,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        CreateTemplateMappingRequest mapping = new CreateTemplateMappingRequest("GameKindId", 99L);
        CreateTemplateSchemaRequest schema =
                new CreateTemplateSchemaRequest(List.of(field), List.of(mapping));
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, schema, null);
        when(templateRepository.existsByWorkspaceIdAndCode(WORKSPACE_ID, TEMPLATE_CODE)).thenReturn(false);
        MasterDataFieldEntity masterDataField = mock(MasterDataFieldEntity.class);
        when(masterDataField.getMasterDataTypeId()).thenReturn(1L);
        when(masterDataFieldRepository.findById(99L)).thenReturn(Optional.of(masterDataField));
        when(masterDataTypeRepository.findByIdAndWorkspaceId(1L, WORKSPACE_ID))
                .thenReturn(Optional.of(mock(MasterDataTypeEntity.class)));
        TemplateEntity persisted = mock(TemplateEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(templateRepository.save(any(TemplateEntity.class))).thenReturn(persisted);
        TemplateFieldEntity savedField = mock(TemplateFieldEntity.class);
        when(savedField.getId()).thenReturn(200L);
        when(templateFieldRepository.save(any(TemplateFieldEntity.class))).thenReturn(savedField);

        templateService.create(request);

        verify(templateFieldRepository).save(any(TemplateFieldEntity.class));
        verify(templateMappingRepository).save(any(TemplateMappingEntity.class));
        verify(templateCompilationOrchestrator).compileAndPersist(10L);
    }

    @Test
    void create_withEmptySchema() {
        CreateTemplateSchemaRequest schema = new CreateTemplateSchemaRequest(List.of(), List.of());
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, schema, null);
        when(templateRepository.existsByWorkspaceIdAndCode(WORKSPACE_ID, TEMPLATE_CODE)).thenReturn(false);
        TemplateEntity persisted = mock(TemplateEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(templateRepository.save(any(TemplateEntity.class))).thenReturn(persisted);

        templateService.create(request);

        ArgumentCaptor<TemplateEntity> captor = ArgumentCaptor.forClass(TemplateEntity.class);
        verify(templateRepository).save(captor.capture());
        TemplateEntity saved = captor.getValue();
        assertThat(saved.getCompiledSchemaJson()).isNull();
        verify(templateFieldRepository, never()).save(any());
        verify(templateMappingRepository, never()).save(any());
        verify(templateCompilationOrchestrator).compileAndPersist(10L);
    }

    @Test
    void transactionRollback_whenSchemaPersistenceFails() {
        CreateTemplateFieldRequest field = new CreateTemplateFieldRequest(
                "GameId",
                null,
                "GameID",
                "Game ID",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        CreateTemplateSchemaRequest schema = new CreateTemplateSchemaRequest(List.of(field), List.of());
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, schema, null);
        when(templateRepository.existsByWorkspaceIdAndCode(WORKSPACE_ID, TEMPLATE_CODE)).thenReturn(false);
        TemplateEntity persisted = mock(TemplateEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(templateRepository.save(any(TemplateEntity.class))).thenReturn(persisted);
        when(templateFieldRepository.save(any(TemplateFieldEntity.class)))
                .thenThrow(new RuntimeException("schema persistence failed"));

        assertThatThrownBy(() -> templateService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("schema persistence failed");

        verify(templateRepository).save(any(TemplateEntity.class));
        verify(templateFieldRepository).save(any(TemplateFieldEntity.class));
        verify(templateCompilationOrchestrator, never()).compileAndPersist(any());
    }

    @Test
    void create_duplicateTemplateCode() {
        CreateTemplateRequest request = new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, null, null);
        TemplateEntity existing =
                new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        when(templateRepository.existsByWorkspaceIdAndCode(WORKSPACE_ID, TEMPLATE_CODE)).thenReturn(true);

        assertThatThrownBy(() -> templateService.create(request))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_CODE_ALREADY_EXISTS);

        verify(templateRepository, never()).save(any());
    }

    @Test
    void update_success() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        TemplateEntity entity = spy(new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID));
        entity.setDescription(DESCRIPTION);
        doReturn(10L).when(entity).getId();
        doReturn(createdAt).when(entity).getCreatedAt();
        doReturn(updatedAt).when(entity).getUpdatedAt();
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));
        when(templateRepository.save(entity)).thenReturn(entity);

        UpdateTemplateRequest request =
                new UpdateTemplateRequest("Player XML Template", "Generate player XML", TemplateStatus.INACTIVE);
        UpdateTemplateResponse response = templateService.update(10L, request);

        assertThat(entity.getCode()).isEqualTo(TEMPLATE_CODE);
        assertThat(entity.getCreatedById()).isEqualTo(USER_ID);
        assertThat(entity.getCompiledSchemaJson()).isNull();
        assertThat(entity.getName()).isEqualTo("Player XML Template");
        assertThat(entity.getDescription()).isEqualTo("Generate player XML");
        assertThat(entity.getStatus()).isEqualTo(TemplateStatus.INACTIVE);
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo(TEMPLATE_CODE);
        assertThat(response.name()).isEqualTo("Player XML Template");
        assertThat(response.description()).isEqualTo("Generate player XML");
        assertThat(response.status()).isEqualTo(TemplateStatus.INACTIVE);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
        verify(templateRepository).findByIdAndWorkspaceId(10L, WORKSPACE_ID);
        verify(templateRepository).save(entity);
    }

    @Test
    void update_notFound() {
        when(templateRepository.findByIdAndWorkspaceId(99L, WORKSPACE_ID)).thenReturn(Optional.empty());

        UpdateTemplateRequest request =
                new UpdateTemplateRequest("Updated Name", "Updated description", TemplateStatus.ACTIVE);
        assertThatThrownBy(() -> templateService.update(99L, request))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);

        verify(templateRepository, never()).save(any());
    }

    @Test
    void delete_success() {
        TemplateEntity entity = new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));

        templateService.delete(10L);

        verify(templateRepository).delete(entity);
    }

    @Test
    void delete_notFound() {
        when(templateRepository.findByIdAndWorkspaceId(99L, WORKSPACE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);

        verify(templateRepository, never()).delete(any());
    }

    @Test
    void updateSchema_success() {
        TemplateEntity entity = new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));

        CreateTemplateFieldRequest titleField = new CreateTemplateFieldRequest(
                "title",
                null,
                "Title",
                "Title",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        CreateTemplateFieldRequest scoreField = new CreateTemplateFieldRequest(
                "score",
                null,
                "Score",
                "Score",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                2,
                null);
        UpdateTemplateSchemaRequest request =
                new UpdateTemplateSchemaRequest(null, List.of(titleField, scoreField), List.of());

        TemplateFieldEntity savedTitle = mock(TemplateFieldEntity.class);
        TemplateFieldEntity savedScore = mock(TemplateFieldEntity.class);
        when(savedTitle.getId()).thenReturn(201L);
        when(savedTitle.getFieldName()).thenReturn("title");
        when(savedTitle.getParentId()).thenReturn(null);
        when(savedTitle.getXmlName()).thenReturn("Title");
        when(savedTitle.getNodeType()).thenReturn(TemplateFieldNodeType.ELEMENT);
        when(savedTitle.getSourceType()).thenReturn(TemplateFieldSourceType.INPUT);
        when(savedTitle.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(savedTitle.getDisplayOrder()).thenReturn(1);
        when(savedScore.getId()).thenReturn(202L);
        when(savedScore.getFieldName()).thenReturn("score");
        when(savedScore.getParentId()).thenReturn(null);
        when(savedScore.getXmlName()).thenReturn("Score");
        when(savedScore.getNodeType()).thenReturn(TemplateFieldNodeType.ELEMENT);
        when(savedScore.getSourceType()).thenReturn(TemplateFieldSourceType.INPUT);
        when(savedScore.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(savedScore.getDisplayOrder()).thenReturn(2);
        when(templateFieldRepository.save(any(TemplateFieldEntity.class)))
                .thenAnswer(invocation -> {
                    TemplateFieldEntity field = invocation.getArgument(0);
                    return "title".equals(field.getFieldName()) ? savedTitle : savedScore;
                });
        when(templateFieldRepository.countByTemplateId(10L)).thenReturn(2L);
        when(templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(savedTitle, savedScore));
        when(templateMappingRepository.findAllByTemplateId(10L)).thenReturn(List.of());

        TemplateSchemaResponse response = templateService.updateSchema(10L, request);

        assertThat(response.version()).isNull();
        assertThat(response.fields()).hasSize(2);
        assertThat(response.fields().get(0).fieldName()).isEqualTo("title");
        assertThat(response.fields().get(1).fieldName()).isEqualTo("score");
        assertThat(response.mappings()).isEmpty();
        verify(templateMappingRepository).deleteByTemplateId(10L);
        verify(templateFieldRepository).deleteByTemplateId(10L);
        verify(templateRepository).findByIdAndWorkspaceId(10L, WORKSPACE_ID);
        verify(templateRepository, never()).save(any());
        verify(templateCompilationOrchestrator).compileAndPersist(10L);
    }

    @Test
    void updateSchema_clearsExistingMetadataWhenEmpty() {
        TemplateEntity entity = new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));
        when(templateFieldRepository.countByTemplateId(10L)).thenReturn(0L);
        when(templateMappingRepository.countByTemplateId(10L)).thenReturn(0L);

        UpdateTemplateSchemaRequest request = new UpdateTemplateSchemaRequest(null, List.of(), List.of());

        TemplateSchemaResponse response = templateService.updateSchema(10L, request);

        assertThat(response).isNull();
        verify(templateMappingRepository).deleteByTemplateId(10L);
        verify(templateFieldRepository).deleteByTemplateId(10L);
        verify(templateFieldRepository, never()).save(any());
        verify(templateCompilationOrchestrator).compileAndPersist(10L);
    }

    @Test
    void updateSchema_validationFailureDoesNotDeleteMetadata() {
        TemplateEntity entity = new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));

        CreateTemplateFieldRequest duplicate = new CreateTemplateFieldRequest(
                "Game",
                null,
                "Game",
                "Game",
                TemplateFieldNodeType.GROUP,
                null,
                null,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        UpdateTemplateSchemaRequest request =
                new UpdateTemplateSchemaRequest(null, List.of(duplicate, duplicate), List.of());

        assertThatThrownBy(() -> templateService.updateSchema(10L, request))
                .isInstanceOf(ValidationException.class);

        verify(templateMappingRepository, never()).deleteByTemplateId(any());
        verify(templateFieldRepository, never()).deleteByTemplateId(any());
        verify(templateCompilationOrchestrator, never()).compileAndPersist(any());
    }

    @Test
    void updateSchema_notFound() {
        when(templateRepository.findByIdAndWorkspaceId(99L, WORKSPACE_ID)).thenReturn(Optional.empty());

        UpdateTemplateSchemaRequest request = new UpdateTemplateSchemaRequest(null, List.of(), List.of());
        assertThatThrownBy(() -> templateService.updateSchema(99L, request))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);

        verify(templateRepository, never()).save(any());
    }

    @Test
    void updateSchema_replaceEntireSchema() {
        TemplateEntity entity = new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));

        CreateTemplateFieldRequest titleField = new CreateTemplateFieldRequest(
                "title",
                null,
                "Title",
                "Title",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        CreateTemplateFieldRequest scoreField = new CreateTemplateFieldRequest(
                "score",
                "title",
                "Score",
                "Score",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        UpdateTemplateSchemaRequest request =
                new UpdateTemplateSchemaRequest(null, List.of(titleField, scoreField), List.of());

        TemplateFieldEntity savedTitle = mock(TemplateFieldEntity.class);
        TemplateFieldEntity savedScore = mock(TemplateFieldEntity.class);
        when(savedTitle.getId()).thenReturn(301L);
        when(savedTitle.getFieldName()).thenReturn("title");
        when(savedTitle.getParentId()).thenReturn(null);
        when(savedTitle.getXmlName()).thenReturn("Title");
        when(savedTitle.getNodeType()).thenReturn(TemplateFieldNodeType.ELEMENT);
        when(savedTitle.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(savedTitle.getDisplayOrder()).thenReturn(1);
        when(savedScore.getId()).thenReturn(302L);
        when(savedScore.getFieldName()).thenReturn("score");
        when(savedScore.getParentId()).thenReturn(301L);
        when(savedScore.getXmlName()).thenReturn("Score");
        when(savedScore.getNodeType()).thenReturn(TemplateFieldNodeType.ELEMENT);
        when(savedScore.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(savedScore.getDisplayOrder()).thenReturn(1);
        when(templateFieldRepository.save(any(TemplateFieldEntity.class)))
                .thenAnswer(invocation -> {
                    TemplateFieldEntity field = invocation.getArgument(0);
                    if ("title".equals(field.getFieldName())) {
                        return savedTitle;
                    }
                    if (field.getParentId() != null) {
                        when(savedScore.getParentId()).thenReturn(301L);
                    }
                    return savedScore;
                });
        when(templateFieldRepository.countByTemplateId(10L)).thenReturn(2L);
        when(templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(savedTitle, savedScore));
        when(templateMappingRepository.findAllByTemplateId(10L)).thenReturn(List.of());

        TemplateSchemaResponse response = templateService.updateSchema(10L, request);

        assertThat(response.fields()).hasSize(2);
        assertThat(response.fields().get(1).parentFieldName()).isEqualTo("title");
        verify(templateMappingRepository).deleteByTemplateId(10L);
        verify(templateFieldRepository).deleteByTemplateId(10L);
        verify(templateCompilationOrchestrator).compileAndPersist(10L);
    }

    @Test
    void updateSchema_duplicateDisplayOrderUnderSameParent_throwsValidationException() {
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID)));

        CreateTemplateFieldRequest first = new CreateTemplateFieldRequest(
                "Game",
                null,
                "Game",
                "Game",
                TemplateFieldNodeType.GROUP,
                null,
                null,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        CreateTemplateFieldRequest duplicateOrder = new CreateTemplateFieldRequest(
                "GameId",
                "Game",
                "GameID",
                "Game ID",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        CreateTemplateFieldRequest duplicateOrderSibling = new CreateTemplateFieldRequest(
                "GameDate",
                "Game",
                "GameDate",
                "Game Date",
                TemplateFieldNodeType.ELEMENT,
                null,
                TemplateFieldSourceType.INPUT,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                null);
        UpdateTemplateSchemaRequest request = new UpdateTemplateSchemaRequest(
                null, List.of(first, duplicateOrder, duplicateOrderSibling), List.of());

        assertThatThrownBy(() -> templateService.updateSchema(10L, request))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> {
                    ValidationException validationException = (ValidationException) ex;
                    assertThat(validationException.getViolations())
                            .anyMatch(v -> TemplateErrorCode.TEMPLATE_DISPLAY_ORDER_DUPLICATE
                                    .code()
                                    .equals(v.code()));
                });

        verify(templateFieldRepository, never()).save(any(TemplateFieldEntity.class));
    }

    @Test
    void findById_withMetadata() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        TemplateEntity entity = spy(new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID));
        entity.setDescription(DESCRIPTION);
        doReturn(10L).when(entity).getId();
        doReturn(createdAt).when(entity).getCreatedAt();
        doReturn(updatedAt).when(entity).getUpdatedAt();
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));
        when(templateFieldRepository.countByTemplateId(10L)).thenReturn(2L);

        TemplateFieldEntity root = mock(TemplateFieldEntity.class);
        TemplateFieldEntity child = mock(TemplateFieldEntity.class);
        when(root.getId()).thenReturn(100L);
        when(root.getFieldName()).thenReturn("Game");
        when(root.getParentId()).thenReturn(null);
        when(root.getXmlName()).thenReturn("Game");
        when(root.getNodeType()).thenReturn(TemplateFieldNodeType.GROUP);
        when(root.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(root.getDisplayOrder()).thenReturn(1);
        when(child.getId()).thenReturn(101L);
        when(child.getFieldName()).thenReturn("GameKindId");
        when(child.getParentId()).thenReturn(100L);
        when(child.getXmlName()).thenReturn("GameKindID");
        when(child.getNodeType()).thenReturn(TemplateFieldNodeType.ELEMENT);
        when(child.getSourceType()).thenReturn(TemplateFieldSourceType.MASTER_DATA);
        when(child.getEmptyHandling()).thenReturn(TemplateFieldEmptyHandling.REQUIRED);
        when(child.getDisplayOrder()).thenReturn(1);
        when(templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(root, child));

        TemplateMappingEntity mapping = new TemplateMappingEntity(10L, 101L, 99L);
        when(templateMappingRepository.findAllByTemplateId(10L)).thenReturn(List.of(mapping));

        TemplateResponse response = templateService.findById(10L);

        assertThat(response.schema()).isNotNull();
        assertThat(response.schema().version()).isNull();
        assertThat(response.schema().fields()).hasSize(2);
        assertThat(response.schema().fields().get(0).fieldName()).isEqualTo("Game");
        assertThat(response.schema().fields().get(0).parentFieldName()).isNull();
        assertThat(response.schema().fields().get(1).fieldName()).isEqualTo("GameKindId");
        assertThat(response.schema().fields().get(1).parentFieldName()).isEqualTo("Game");
        assertThat(response.schema().mappings()).hasSize(1);
        assertThat(response.schema().mappings().get(0).fieldName()).isEqualTo("GameKindId");
        assertThat(response.schema().mappings().get(0).masterDataFieldId()).isEqualTo(99L);
        verify(templateFieldRepository).findAllByTemplateIdOrderByDisplayOrderAsc(10L);
        verify(templateMappingRepository).findAllByTemplateId(10L);
    }

    @Test
    void findById_ignoresCompiledSchemaJson() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        TemplateEntity entity = spy(new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID));
        doReturn(10L).when(entity).getId();
        doReturn(createdAt).when(entity).getCreatedAt();
        doReturn(updatedAt).when(entity).getUpdatedAt();
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));
        when(templateFieldRepository.countByTemplateId(10L)).thenReturn(0L);
        when(templateMappingRepository.countByTemplateId(10L)).thenReturn(0L);

        TemplateResponse response = templateService.findById(10L);

        assertThat(response.schema()).isNull();
    }

    @Test
    void findById_withoutSchema() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        TemplateEntity entity = spy(new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID));
        entity.setDescription(DESCRIPTION);
        doReturn(10L).when(entity).getId();
        doReturn(createdAt).when(entity).getCreatedAt();
        doReturn(updatedAt).when(entity).getUpdatedAt();
        when(templateRepository.findByIdAndWorkspaceId(10L, WORKSPACE_ID)).thenReturn(Optional.of(entity));
        when(templateFieldRepository.countByTemplateId(10L)).thenReturn(0L);
        when(templateMappingRepository.countByTemplateId(10L)).thenReturn(0L);

        TemplateResponse response = templateService.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo(TEMPLATE_CODE);
        assertThat(response.schema()).isNull();
        verify(templateRepository).findByIdAndWorkspaceId(10L, WORKSPACE_ID);
    }

    @Test
    void findById_notFound() {
        when(templateRepository.findByIdAndWorkspaceId(99L, WORKSPACE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);

        verify(templateRepository).findByIdAndWorkspaceId(99L, WORKSPACE_ID);
    }

    @Test
    void list_success() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        TemplateEntity entity = mock(TemplateEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getCode()).thenReturn(TEMPLATE_CODE);
        when(entity.getName()).thenReturn(TEMPLATE_NAME);
        when(entity.getDescription()).thenReturn(DESCRIPTION);
        when(entity.getStatus()).thenReturn(TemplateStatus.ACTIVE);
        when(entity.getCreatedAt()).thenReturn(createdAt);
        when(entity.getUpdatedAt()).thenReturn(updatedAt);
        Page<TemplateEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);
        when(templateRepository.searchByWorkspace(eq(WORKSPACE_ID), eq(null), eq(null), any(Pageable.class))).thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, null, null);

        assertThat(result.content()).hasSize(1);
        TemplateListResponse item = result.content().get(0);
        assertThat(item.id()).isEqualTo(1L);
        assertThat(item.code()).isEqualTo(TEMPLATE_CODE);
        assertThat(item.name()).isEqualTo(TEMPLATE_NAME);
        assertThat(item.description()).isEqualTo(DESCRIPTION);
        assertThat(item.status()).isEqualTo(TemplateStatus.ACTIVE);
        assertThat(item.createdAt()).isEqualTo(createdAt);
        assertThat(item.updatedAt()).isEqualTo(updatedAt);
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.meta().pageSize()).isEqualTo(20);
        assertThat(result.meta().totalRecords()).isEqualTo(1);
        assertThat(result.meta().totalPages()).isEqualTo(1);
        verify(templateRepository).searchByWorkspace(eq(WORKSPACE_ID), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void list_empty() {
        Page<TemplateEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(templateRepository.searchByWorkspace(eq(WORKSPACE_ID), eq(null), eq(null), any(Pageable.class))).thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, "", null);

        assertThat(result.content()).isEmpty();
        assertThat(result.meta().totalRecords()).isZero();
        assertThat(result.meta().totalPages()).isZero();
        verify(templateRepository).searchByWorkspace(eq(WORKSPACE_ID), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void list_keyword() {
        Page<TemplateEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(templateRepository.searchByWorkspace(eq(WORKSPACE_ID), eq("Live"), eq(null), any(Pageable.class))).thenReturn(page);

        templateService.findAll(1, 20, "  Live  ", null);

        verify(templateRepository).searchByWorkspace(eq(WORKSPACE_ID), eq("Live"), eq(null), any(Pageable.class));
    }

    @Test
    void list_active() {
        TemplateEntity active = mockListEntity(1L, "ACTIVE_CODE", TemplateStatus.ACTIVE);
        Page<TemplateEntity> page = new PageImpl<>(List.of(active), PageRequest.of(0, 20), 1);
        when(templateRepository.searchByWorkspace(eq(WORKSPACE_ID), eq(null), eq(TemplateStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, null, TemplateStatus.ACTIVE);

        assertThat(result.content()).allMatch(item -> item.status() == TemplateStatus.ACTIVE);
        assertThat(result.content()).noneMatch(item -> item.status() == TemplateStatus.INACTIVE);
        verify(templateRepository).searchByWorkspace(eq(WORKSPACE_ID), eq(null), eq(TemplateStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void list_inactive() {
        TemplateEntity inactive = mockListEntity(2L, "INACTIVE_CODE", TemplateStatus.INACTIVE);
        Page<TemplateEntity> page = new PageImpl<>(List.of(inactive), PageRequest.of(0, 20), 1);
        when(templateRepository.searchByWorkspace(eq(WORKSPACE_ID), eq(null), eq(TemplateStatus.INACTIVE), any(Pageable.class)))
                .thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, null, TemplateStatus.INACTIVE);

        assertThat(result.content()).allMatch(item -> item.status() == TemplateStatus.INACTIVE);
        assertThat(result.content()).noneMatch(item -> item.status() == TemplateStatus.ACTIVE);
        verify(templateRepository).searchByWorkspace(eq(WORKSPACE_ID), eq(null), eq(TemplateStatus.INACTIVE), any(Pageable.class));
    }

    @Test
    void list_keywordAndActive() {
        TemplateEntity active = mockListEntity(3L, "GAME_TEMPLATE", TemplateStatus.ACTIVE);
        Page<TemplateEntity> page = new PageImpl<>(List.of(active), PageRequest.of(0, 20), 1);
        when(templateRepository.searchByWorkspace(eq(WORKSPACE_ID), eq("game"), eq(TemplateStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, "game", TemplateStatus.ACTIVE);

        assertThat(result.content()).allMatch(item -> item.status() == TemplateStatus.ACTIVE);
        assertThat(result.content()).noneMatch(item -> item.status() == TemplateStatus.INACTIVE);
        verify(templateRepository).searchByWorkspace(eq(WORKSPACE_ID), eq("game"), eq(TemplateStatus.ACTIVE), any(Pageable.class));
    }

    private TemplateEntity mockListEntity(Long id, String code, TemplateStatus status) {
        TemplateEntity entity = mock(TemplateEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getCode()).thenReturn(code);
        when(entity.getName()).thenReturn(TEMPLATE_NAME);
        when(entity.getDescription()).thenReturn(DESCRIPTION);
        when(entity.getStatus()).thenReturn(status);
        when(entity.getCreatedAt()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(entity.getUpdatedAt()).thenReturn(Instant.parse("2026-01-02T00:00:00Z"));
        return entity;
    }
}
