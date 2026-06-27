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
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateListResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateSchemaResponse;
import com.company.xmlgen.template.dto.response.UpdateTemplateResponse;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

    @Mock
    private TemplateRepository templateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TemplateServiceImpl templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateServiceImpl(templateRepository, objectMapper);
        AuthenticatedUser currentUser = new AuthenticatedUser(USER_ID, "admin", true);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_withoutSchema() {
        CreateTemplateRequest request = new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, null);
        when(templateRepository.findByCode(TEMPLATE_CODE)).thenReturn(Optional.empty());
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
    }

    @Test
    void create_withSchema() {
        JsonNode titleField = JsonNodeFactory.instance.objectNode().put("name", "title").put("type", "STRING");
        TemplateSchemaResponse schema = new TemplateSchemaResponse(1L, List.of(titleField), List.of());
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, schema);
        when(templateRepository.findByCode(TEMPLATE_CODE)).thenReturn(Optional.empty());
        TemplateEntity persisted = mock(TemplateEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(templateRepository.save(any(TemplateEntity.class))).thenReturn(persisted);

        CreateTemplateResponse response = templateService.create(request);

        assertThat(response.id()).isEqualTo(10L);

        ArgumentCaptor<TemplateEntity> captor = ArgumentCaptor.forClass(TemplateEntity.class);
        verify(templateRepository).save(captor.capture());
        TemplateEntity saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo(TEMPLATE_CODE);
        assertThat(saved.getCompiledSchemaJson()).isNotNull();
        assertThat(saved.getCompiledSchemaJson().get("version").asLong()).isEqualTo(1L);
        assertThat(saved.getCompiledSchemaJson().get("fields")).hasSize(1);
        assertThat(saved.getCompiledSchemaJson().get("fields").get(0).get("name").asText())
                .isEqualTo("title");
        assertThat(saved.getCompiledSchemaJson().get("mappings")).isEmpty();
    }

    @Test
    void create_withEmptySchema() {
        TemplateSchemaResponse schema = new TemplateSchemaResponse(1L, List.of(), List.of());
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, schema);
        when(templateRepository.findByCode(TEMPLATE_CODE)).thenReturn(Optional.empty());
        TemplateEntity persisted = mock(TemplateEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(templateRepository.save(any(TemplateEntity.class))).thenReturn(persisted);

        templateService.create(request);

        ArgumentCaptor<TemplateEntity> captor = ArgumentCaptor.forClass(TemplateEntity.class);
        verify(templateRepository).save(captor.capture());
        TemplateEntity saved = captor.getValue();
        assertThat(saved.getCompiledSchemaJson()).isNotNull();
        assertThat(saved.getCompiledSchemaJson().get("version").asLong()).isEqualTo(1L);
        assertThat(saved.getCompiledSchemaJson().get("fields")).isEmpty();
        assertThat(saved.getCompiledSchemaJson().get("mappings")).isEmpty();
    }

    @Test
    void transactionRollback_whenSchemaPersistenceFails() {
        JsonNode titleField = JsonNodeFactory.instance.objectNode().put("name", "title").put("type", "STRING");
        TemplateSchemaResponse schema = new TemplateSchemaResponse(1L, List.of(titleField), List.of());
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, schema);
        when(templateRepository.findByCode(TEMPLATE_CODE)).thenReturn(Optional.empty());
        when(templateRepository.save(any(TemplateEntity.class)))
                .thenThrow(new RuntimeException("schema persistence failed"));

        assertThatThrownBy(() -> templateService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("schema persistence failed");

        verify(templateRepository).save(any(TemplateEntity.class));
    }

    @Test
    void create_duplicateTemplateCode() {
        CreateTemplateRequest request = new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION, null);
        TemplateEntity existing =
                new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        when(templateRepository.findByCode(TEMPLATE_CODE)).thenReturn(Optional.of(existing));

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
        when(templateRepository.findById(10L)).thenReturn(Optional.of(entity));
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
        verify(templateRepository).findById(10L);
        verify(templateRepository).save(entity);
    }

    @Test
    void update_notFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

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
        when(templateRepository.findById(10L)).thenReturn(Optional.of(entity));

        templateService.delete(10L);

        verify(templateRepository).delete(entity);
    }

    @Test
    void delete_notFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);

        verify(templateRepository, never()).delete(any());
    }

    @Test
    void updateSchema_success() {
        JsonNode titleField = JsonNodeFactory.instance.objectNode().put("name", "title").put("type", "STRING");
        JsonNode scoreField = JsonNodeFactory.instance.objectNode().put("name", "score").put("type", "INTEGER");
        TemplateEntity entity = new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        when(templateRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(templateRepository.save(entity)).thenReturn(entity);

        UpdateTemplateSchemaRequest request =
                new UpdateTemplateSchemaRequest(2L, List.of(titleField, scoreField), List.of());
        TemplateSchemaResponse response = templateService.updateSchema(10L, request);

        assertThat(response.version()).isEqualTo(2L);
        assertThat(response.fields()).hasSize(2);
        assertThat(response.mappings()).isEmpty();
        assertThat(entity.getCompiledSchemaJson().get("version").asLong()).isEqualTo(2L);
        assertThat(entity.getCompiledSchemaJson().get("fields")).hasSize(2);
        verify(templateRepository).findById(10L);
        verify(templateRepository).save(entity);
    }

    @Test
    void updateSchema_notFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateTemplateSchemaRequest request = new UpdateTemplateSchemaRequest(1L, List.of(), List.of());
        assertThatThrownBy(() -> templateService.updateSchema(99L, request))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);

        verify(templateRepository, never()).save(any());
    }

    @Test
    void updateSchema_replaceEntireSchema() {
        JsonNode oldField = JsonNodeFactory.instance.objectNode().put("name", "legacy").put("type", "STRING");
        TemplateSchemaResponse oldSchema = new TemplateSchemaResponse(1L, List.of(oldField), List.of());
        TemplateEntity entity = new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID);
        entity.setDescription(DESCRIPTION);
        entity.setCompiledSchemaJson(objectMapper.valueToTree(oldSchema));
        when(templateRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(templateRepository.save(entity)).thenReturn(entity);

        JsonNode titleField = JsonNodeFactory.instance.objectNode().put("name", "title").put("type", "STRING");
        JsonNode scoreField = JsonNodeFactory.instance.objectNode().put("name", "score").put("type", "INTEGER");
        UpdateTemplateSchemaRequest request =
                new UpdateTemplateSchemaRequest(2L, List.of(titleField, scoreField), List.of());

        templateService.updateSchema(10L, request);

        assertThat(entity.getCode()).isEqualTo(TEMPLATE_CODE);
        assertThat(entity.getName()).isEqualTo(TEMPLATE_NAME);
        assertThat(entity.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(entity.getStatus()).isEqualTo(TemplateStatus.ACTIVE);
        assertThat(entity.getCompiledSchemaJson().get("version").asLong()).isEqualTo(2L);
        assertThat(entity.getCompiledSchemaJson().get("fields")).hasSize(2);
        assertThat(entity.getCompiledSchemaJson().get("fields").get(0).get("name").asText())
                .isEqualTo("title");
        assertThat(entity.getCompiledSchemaJson().get("fields").get(1).get("name").asText())
                .isEqualTo("score");
        verify(templateRepository).save(entity);
    }

    @Test
    void findById_withSchema() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        JsonNode titleField = JsonNodeFactory.instance.objectNode().put("name", "title").put("type", "STRING");
        TemplateSchemaResponse stored = new TemplateSchemaResponse(1L, List.of(titleField), List.of());
        TemplateEntity entity = spy(new TemplateEntity(TEMPLATE_CODE, TEMPLATE_NAME, TemplateStatus.ACTIVE, USER_ID));
        entity.setDescription(DESCRIPTION);
        entity.setCompiledSchemaJson(objectMapper.valueToTree(stored));
        doReturn(10L).when(entity).getId();
        doReturn(createdAt).when(entity).getCreatedAt();
        doReturn(updatedAt).when(entity).getUpdatedAt();
        when(templateRepository.findById(10L)).thenReturn(Optional.of(entity));

        TemplateResponse response = templateService.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo(TEMPLATE_CODE);
        assertThat(response.name()).isEqualTo(TEMPLATE_NAME);
        assertThat(response.description()).isEqualTo(DESCRIPTION);
        assertThat(response.status()).isEqualTo(TemplateStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
        assertThat(response.schema()).isNotNull();
        assertThat(response.schema().version()).isEqualTo(1L);
        assertThat(response.schema().fields()).hasSize(1);
        assertThat(response.schema().fields().get(0).get("name").asText()).isEqualTo("title");
        assertThat(response.schema().mappings()).isEmpty();
        verify(templateRepository).findById(10L);
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
        when(templateRepository.findById(10L)).thenReturn(Optional.of(entity));

        TemplateResponse response = templateService.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.code()).isEqualTo(TEMPLATE_CODE);
        assertThat(response.schema()).isNull();
        verify(templateRepository).findById(10L);
    }

    @Test
    void findById_notFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);

        verify(templateRepository).findById(99L);
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
        when(templateRepository.search(eq(null), eq(null), any(Pageable.class))).thenReturn(page);

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
        verify(templateRepository).search(eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void list_empty() {
        Page<TemplateEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(templateRepository.search(eq(null), eq(null), any(Pageable.class))).thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, "", null);

        assertThat(result.content()).isEmpty();
        assertThat(result.meta().totalRecords()).isZero();
        assertThat(result.meta().totalPages()).isZero();
        verify(templateRepository).search(eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void list_keyword() {
        Page<TemplateEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(templateRepository.search(eq("Live"), eq(null), any(Pageable.class))).thenReturn(page);

        templateService.findAll(1, 20, "  Live  ", null);

        verify(templateRepository).search(eq("Live"), eq(null), any(Pageable.class));
    }

    @Test
    void list_active() {
        TemplateEntity active = mockListEntity(1L, "ACTIVE_CODE", TemplateStatus.ACTIVE);
        Page<TemplateEntity> page = new PageImpl<>(List.of(active), PageRequest.of(0, 20), 1);
        when(templateRepository.search(eq(null), eq(TemplateStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, null, TemplateStatus.ACTIVE);

        assertThat(result.content()).allMatch(item -> item.status() == TemplateStatus.ACTIVE);
        assertThat(result.content()).noneMatch(item -> item.status() == TemplateStatus.INACTIVE);
        verify(templateRepository).search(eq(null), eq(TemplateStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void list_inactive() {
        TemplateEntity inactive = mockListEntity(2L, "INACTIVE_CODE", TemplateStatus.INACTIVE);
        Page<TemplateEntity> page = new PageImpl<>(List.of(inactive), PageRequest.of(0, 20), 1);
        when(templateRepository.search(eq(null), eq(TemplateStatus.INACTIVE), any(Pageable.class)))
                .thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, null, TemplateStatus.INACTIVE);

        assertThat(result.content()).allMatch(item -> item.status() == TemplateStatus.INACTIVE);
        assertThat(result.content()).noneMatch(item -> item.status() == TemplateStatus.ACTIVE);
        verify(templateRepository).search(eq(null), eq(TemplateStatus.INACTIVE), any(Pageable.class));
    }

    @Test
    void list_keywordAndActive() {
        TemplateEntity active = mockListEntity(3L, "GAME_TEMPLATE", TemplateStatus.ACTIVE);
        Page<TemplateEntity> page = new PageImpl<>(List.of(active), PageRequest.of(0, 20), 1);
        when(templateRepository.search(eq("game"), eq(TemplateStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, "game", TemplateStatus.ACTIVE);

        assertThat(result.content()).allMatch(item -> item.status() == TemplateStatus.ACTIVE);
        assertThat(result.content()).noneMatch(item -> item.status() == TemplateStatus.INACTIVE);
        verify(templateRepository).search(eq("game"), eq(TemplateStatus.ACTIVE), any(Pageable.class));
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
