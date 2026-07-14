package com.company.xmlgen.masterdata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.response.CreateMasterDataFieldResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataFieldListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataFieldResponse;
import com.company.xmlgen.masterdata.dto.response.UpdateMasterDataFieldResponse;
import com.company.xmlgen.masterdata.entity.MasterDataFieldDataType;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.exception.MasterDataFieldErrorCode;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
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

@ExtendWith(MockitoExtension.class)
class MasterDataFieldServiceImplTest {

    private static final Long WORKSPACE_ID = 1L;
    private static final Long TYPE_ID = 1L;
    private static final String TYPE_CODE = "GAME_KIND";
    private static final String TYPE_NAME = "Game Kind";
    private static final String FIELD_CODE = "game_kind_name";
    private static final String FIELD_NAME = "Game Kind Name";
    private static final String FIELD_DESCRIPTION = "Help text for game kind name";
    private static final String FIELD_DEFAULT_VALUE = "J1";
    private static final Long REFERENCE_TYPE_ID = 5L;

    @Mock
    private MasterDataFieldRepository masterDataFieldRepository;

    @Mock
    private MasterDataTypeRepository masterDataTypeRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private MasterDataRecordRepository masterDataRecordRepository;

    private MasterDataFieldServiceImpl masterDataFieldService;

    @BeforeEach
    void setUp() {
        WorkspaceOwnershipGuard workspaceOwnershipGuard = new WorkspaceOwnershipGuard(
                templateRepository,
                masterDataTypeRepository,
                masterDataFieldRepository,
                masterDataRecordRepository);
        masterDataFieldService = new MasterDataFieldServiceImpl(
                masterDataFieldRepository,
                masterDataTypeRepository,
                workspaceOwnershipGuard,
                org.mockito.Mockito.mock(com.company.xmlgen.workspace.service.UserPermissionGuard.class));
        WorkspaceTestSupport.useDefaultWorkspace();
    }

    @AfterEach
    void tearDown() {
        WorkspaceTestSupport.clearWorkspace();
    }

    private void stubTypeInWorkspace(Long typeId) {
        when(masterDataTypeRepository.findByIdAndWorkspaceId(typeId, WORKSPACE_ID))
                .thenReturn(Optional.of(mock(MasterDataTypeEntity.class)));
    }

    private void stubTypeNotInWorkspace(Long typeId) {
        when(masterDataTypeRepository.findByIdAndWorkspaceId(typeId, WORKSPACE_ID))
                .thenReturn(Optional.empty());
    }

    private void stubFieldInWorkspace(Long fieldId, MasterDataFieldEntity entity) {
        when(masterDataFieldRepository.findById(fieldId)).thenReturn(Optional.of(entity));
        stubTypeInWorkspace(entity.getMasterDataTypeId());
    }

    @Test
    void create_success() {
        CreateMasterDataFieldRequest request = new CreateMasterDataFieldRequest(
                TYPE_ID,
                FIELD_CODE,
                FIELD_NAME,
                MasterDataFieldDataType.STRING,
                true,
                1,
                FIELD_DESCRIPTION,
                FIELD_DEFAULT_VALUE,
                true,
                false,
                REFERENCE_TYPE_ID);
        stubTypeInWorkspace(TYPE_ID);
        when(masterDataFieldRepository.existsByMasterDataTypeIdAndFieldName(TYPE_ID, FIELD_CODE))
                .thenReturn(false);
        when(masterDataFieldRepository.existsByMasterDataTypeIdAndDisplayOrder(TYPE_ID, 1))
                .thenReturn(false);
        MasterDataFieldEntity persisted = mock(MasterDataFieldEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(persisted.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(persisted.getFieldName()).thenReturn(FIELD_CODE);
        when(persisted.getName()).thenReturn(FIELD_NAME);
        when(persisted.getDataType()).thenReturn(MasterDataFieldDataType.STRING);
        when(persisted.isRequired()).thenReturn(true);
        when(persisted.getDisplayOrder()).thenReturn(1);
        when(persisted.getDescription()).thenReturn(FIELD_DESCRIPTION);
        when(persisted.getDefaultValue()).thenReturn(FIELD_DEFAULT_VALUE);
        when(persisted.isUnique()).thenReturn(true);
        when(persisted.isSearchable()).thenReturn(false);
        when(persisted.getMasterDataReferenceTypeId()).thenReturn(REFERENCE_TYPE_ID);
        when(masterDataFieldRepository.save(any(MasterDataFieldEntity.class))).thenReturn(persisted);

        CreateMasterDataFieldResponse response = masterDataFieldService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.typeId()).isEqualTo(TYPE_ID);
        assertThat(response.code()).isEqualTo(FIELD_CODE);
        assertThat(response.name()).isEqualTo(FIELD_NAME);
        assertThat(response.dataType()).isEqualTo(MasterDataFieldDataType.STRING);
        assertThat(response.required()).isTrue();
        assertThat(response.displayOrder()).isEqualTo(1);
        assertThat(response.description()).isEqualTo(FIELD_DESCRIPTION);
        assertThat(response.defaultValue()).isEqualTo(FIELD_DEFAULT_VALUE);
        assertThat(response.unique()).isTrue();
        assertThat(response.searchable()).isFalse();
        assertThat(response.masterDataReferenceTypeId()).isEqualTo(REFERENCE_TYPE_ID);

        verify(masterDataTypeRepository).findByIdAndWorkspaceId(TYPE_ID, WORKSPACE_ID);
        verify(masterDataFieldRepository).existsByMasterDataTypeIdAndFieldName(TYPE_ID, FIELD_CODE);
        verify(masterDataFieldRepository).existsByMasterDataTypeIdAndDisplayOrder(TYPE_ID, 1);

        ArgumentCaptor<MasterDataFieldEntity> captor = ArgumentCaptor.forClass(MasterDataFieldEntity.class);
        verify(masterDataFieldRepository).save(captor.capture());
        MasterDataFieldEntity saved = captor.getValue();
        assertThat(saved.getMasterDataTypeId()).isEqualTo(TYPE_ID);
        assertThat(saved.getFieldName()).isEqualTo(FIELD_CODE);
        assertThat(saved.getName()).isEqualTo(FIELD_NAME);
        assertThat(saved.getDataType()).isEqualTo(MasterDataFieldDataType.STRING);
        assertThat(saved.isRequired()).isTrue();
        assertThat(saved.getDisplayOrder()).isEqualTo(1);
        assertThat(saved.getDescription()).isEqualTo(FIELD_DESCRIPTION);
        assertThat(saved.getDefaultValue()).isEqualTo(FIELD_DEFAULT_VALUE);
        assertThat(saved.isUnique()).isTrue();
        assertThat(saved.isSearchable()).isFalse();
        assertThat(saved.getMasterDataReferenceTypeId()).isEqualTo(REFERENCE_TYPE_ID);
    }

    @Test
    void create_typeNotFound() {
        CreateMasterDataFieldRequest request = new CreateMasterDataFieldRequest(
                99L,
                FIELD_CODE,
                FIELD_NAME,
                MasterDataFieldDataType.STRING,
                true,
                1,
                null,
                null,
                null,
                null,
                null);
        stubTypeNotInWorkspace(99L);

        assertThatThrownBy(() -> masterDataFieldService.create(request))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND);

        verify(masterDataTypeRepository).findByIdAndWorkspaceId(99L, WORKSPACE_ID);
        verify(masterDataFieldRepository, never()).save(any());
    }

    @Test
    void create_duplicateCode() {
        CreateMasterDataFieldRequest request = new CreateMasterDataFieldRequest(
                TYPE_ID,
                FIELD_CODE,
                FIELD_NAME,
                MasterDataFieldDataType.STRING,
                true,
                1,
                null,
                null,
                null,
                null,
                null);
        stubTypeInWorkspace(TYPE_ID);
        when(masterDataFieldRepository.existsByMasterDataTypeIdAndFieldName(TYPE_ID, FIELD_CODE))
                .thenReturn(true);

        assertThatThrownBy(() -> masterDataFieldService.create(request))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(MasterDataFieldErrorCode.MASTER_DATA_FIELD_CODE_ALREADY_EXISTS);

        verify(masterDataFieldRepository).existsByMasterDataTypeIdAndFieldName(TYPE_ID, FIELD_CODE);
        verify(masterDataFieldRepository, never()).save(any());
    }

    @Test
    void create_duplicateDisplayOrder() {
        CreateMasterDataFieldRequest request = new CreateMasterDataFieldRequest(
                TYPE_ID,
                FIELD_CODE,
                FIELD_NAME,
                MasterDataFieldDataType.STRING,
                true,
                1,
                null,
                null,
                null,
                null,
                null);
        stubTypeInWorkspace(TYPE_ID);
        when(masterDataFieldRepository.existsByMasterDataTypeIdAndFieldName(TYPE_ID, FIELD_CODE))
                .thenReturn(false);
        when(masterDataFieldRepository.existsByMasterDataTypeIdAndDisplayOrder(TYPE_ID, 1))
                .thenReturn(true);

        assertThatThrownBy(() -> masterDataFieldService.create(request))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(MasterDataFieldErrorCode.DISPLAY_ORDER_ALREADY_EXISTS);

        verify(masterDataFieldRepository).existsByMasterDataTypeIdAndDisplayOrder(TYPE_ID, 1);
        verify(masterDataFieldRepository, never()).save(any());
    }

    @Test
    void update_success() {
        MasterDataFieldEntity entity = new MasterDataFieldEntity(
                TYPE_ID, FIELD_CODE, FIELD_NAME, MasterDataFieldDataType.STRING, true, 1);
        stubFieldInWorkspace(1L, entity);
        when(masterDataFieldRepository.existsByMasterDataTypeIdAndDisplayOrderAndIdNot(TYPE_ID, 2, 1L))
                .thenReturn(false);
        when(masterDataFieldRepository.save(entity)).thenReturn(entity);
        MasterDataTypeEntity type = mock(MasterDataTypeEntity.class);
        when(type.getCode()).thenReturn(TYPE_CODE);
        when(type.getName()).thenReturn(TYPE_NAME);
        when(masterDataTypeRepository.findByIdAndWorkspaceId(TYPE_ID, WORKSPACE_ID)).thenReturn(Optional.of(type));

        UpdateMasterDataFieldRequest request = new UpdateMasterDataFieldRequest(
                "Game Kind Name Updated",
                MasterDataFieldDataType.STRING,
                true,
                2,
                FIELD_DESCRIPTION,
                FIELD_DEFAULT_VALUE,
                true,
                false,
                REFERENCE_TYPE_ID);
        UpdateMasterDataFieldResponse response = masterDataFieldService.update(1L, request);

        assertThat(entity.getFieldName()).isEqualTo(FIELD_CODE);
        assertThat(entity.getMasterDataTypeId()).isEqualTo(TYPE_ID);
        assertThat(entity.getName()).isEqualTo("Game Kind Name Updated");
        assertThat(entity.getDataType()).isEqualTo(MasterDataFieldDataType.STRING);
        assertThat(entity.isRequired()).isTrue();
        assertThat(entity.getDisplayOrder()).isEqualTo(2);
        assertThat(entity.getDescription()).isEqualTo(FIELD_DESCRIPTION);
        assertThat(entity.getDefaultValue()).isEqualTo(FIELD_DEFAULT_VALUE);
        assertThat(entity.isUnique()).isTrue();
        assertThat(entity.isSearchable()).isFalse();
        assertThat(entity.getMasterDataReferenceTypeId()).isEqualTo(REFERENCE_TYPE_ID);
        assertThat(response.code()).isEqualTo(FIELD_CODE);
        assertThat(response.typeId()).isEqualTo(TYPE_ID);
        assertThat(response.typeCode()).isEqualTo(TYPE_CODE);
        assertThat(response.typeName()).isEqualTo(TYPE_NAME);
        assertThat(response.name()).isEqualTo("Game Kind Name Updated");
        assertThat(response.displayOrder()).isEqualTo(2);
        assertThat(response.description()).isEqualTo(FIELD_DESCRIPTION);
        assertThat(response.defaultValue()).isEqualTo(FIELD_DEFAULT_VALUE);
        assertThat(response.unique()).isTrue();
        assertThat(response.searchable()).isFalse();
        assertThat(response.masterDataReferenceTypeId()).isEqualTo(REFERENCE_TYPE_ID);
        verify(masterDataFieldRepository).findById(1L);
        verify(masterDataFieldRepository).existsByMasterDataTypeIdAndDisplayOrderAndIdNot(TYPE_ID, 2, 1L);
        verify(masterDataFieldRepository).save(entity);
        verify(masterDataTypeRepository, times(2)).findByIdAndWorkspaceId(TYPE_ID, WORKSPACE_ID);
    }

    @Test
    void update_notFound() {
        when(masterDataFieldRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateMasterDataFieldRequest request = new UpdateMasterDataFieldRequest(
                "Game Kind Name Updated", MasterDataFieldDataType.STRING, true, 2, null, null, false, false, null);
        assertThatThrownBy(() -> masterDataFieldService.update(99L, request))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(MasterDataFieldErrorCode.MASTER_DATA_FIELD_NOT_FOUND);

        verify(masterDataFieldRepository).findById(99L);
        verify(masterDataFieldRepository, never()).save(any());
    }

    @Test
    void update_duplicateDisplayOrder() {
        MasterDataFieldEntity entity = new MasterDataFieldEntity(
                TYPE_ID, FIELD_CODE, FIELD_NAME, MasterDataFieldDataType.STRING, true, 1);
        stubFieldInWorkspace(1L, entity);
        when(masterDataFieldRepository.existsByMasterDataTypeIdAndDisplayOrderAndIdNot(TYPE_ID, 2, 1L))
                .thenReturn(true);

        UpdateMasterDataFieldRequest request = new UpdateMasterDataFieldRequest(
                "Game Kind Name Updated", MasterDataFieldDataType.STRING, true, 2, null, null, false, false, null);
        assertThatThrownBy(() -> masterDataFieldService.update(1L, request))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(MasterDataFieldErrorCode.DISPLAY_ORDER_ALREADY_EXISTS);

        verify(masterDataFieldRepository).existsByMasterDataTypeIdAndDisplayOrderAndIdNot(TYPE_ID, 2, 1L);
        verify(masterDataFieldRepository, never()).save(any());
    }

    @Test
    void update_sameDisplayOrder_success() {
        MasterDataFieldEntity entity = new MasterDataFieldEntity(
                TYPE_ID, FIELD_CODE, FIELD_NAME, MasterDataFieldDataType.STRING, true, 1);
        stubFieldInWorkspace(1L, entity);
        when(masterDataFieldRepository.existsByMasterDataTypeIdAndDisplayOrderAndIdNot(TYPE_ID, 1, 1L))
                .thenReturn(false);
        when(masterDataFieldRepository.save(entity)).thenReturn(entity);
        MasterDataTypeEntity type = mock(MasterDataTypeEntity.class);
        when(type.getCode()).thenReturn(TYPE_CODE);
        when(type.getName()).thenReturn(TYPE_NAME);
        when(masterDataTypeRepository.findByIdAndWorkspaceId(TYPE_ID, WORKSPACE_ID)).thenReturn(Optional.of(type));

        UpdateMasterDataFieldRequest request = new UpdateMasterDataFieldRequest(
                "Game Kind Name Updated", MasterDataFieldDataType.STRING, true, 1, null, null, false, false, null);
        UpdateMasterDataFieldResponse response = masterDataFieldService.update(1L, request);

        assertThat(entity.getDisplayOrder()).isEqualTo(1);
        assertThat(response.displayOrder()).isEqualTo(1);
        verify(masterDataFieldRepository).existsByMasterDataTypeIdAndDisplayOrderAndIdNot(TYPE_ID, 1, 1L);
        verify(masterDataFieldRepository).save(entity);
    }

    @Test
    void delete_success() {
        MasterDataFieldEntity entity = new MasterDataFieldEntity(
                TYPE_ID, FIELD_CODE, FIELD_NAME, MasterDataFieldDataType.STRING, true, 1);
        stubFieldInWorkspace(1L, entity);

        masterDataFieldService.delete(1L);

        verify(masterDataFieldRepository).findById(1L);
        verify(masterDataFieldRepository).delete(entity);
    }

    @Test
    void delete_notFound() {
        when(masterDataFieldRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterDataFieldService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(MasterDataFieldErrorCode.MASTER_DATA_FIELD_NOT_FOUND);

        verify(masterDataFieldRepository).findById(99L);
        verify(masterDataFieldRepository, never()).delete(any());
    }

    @Test
    void findById_success() {
        MasterDataFieldEntity entity = mock(MasterDataFieldEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(entity.getFieldName()).thenReturn(FIELD_CODE);
        when(entity.getName()).thenReturn(FIELD_NAME);
        when(entity.getDataType()).thenReturn(MasterDataFieldDataType.STRING);
        when(entity.isRequired()).thenReturn(true);
        when(entity.getDisplayOrder()).thenReturn(1);
        when(entity.getDescription()).thenReturn(FIELD_DESCRIPTION);
        when(entity.getDefaultValue()).thenReturn(FIELD_DEFAULT_VALUE);
        when(entity.isUnique()).thenReturn(true);
        when(entity.isSearchable()).thenReturn(false);
        when(entity.getMasterDataReferenceTypeId()).thenReturn(REFERENCE_TYPE_ID);
        stubFieldInWorkspace(1L, entity);

        MasterDataFieldResponse response = masterDataFieldService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.typeId()).isEqualTo(TYPE_ID);
        assertThat(response.code()).isEqualTo(FIELD_CODE);
        assertThat(response.name()).isEqualTo(FIELD_NAME);
        assertThat(response.dataType()).isEqualTo(MasterDataFieldDataType.STRING);
        assertThat(response.required()).isTrue();
        assertThat(response.displayOrder()).isEqualTo(1);
        assertThat(response.description()).isEqualTo(FIELD_DESCRIPTION);
        assertThat(response.defaultValue()).isEqualTo(FIELD_DEFAULT_VALUE);
        assertThat(response.unique()).isTrue();
        assertThat(response.searchable()).isFalse();
        assertThat(response.masterDataReferenceTypeId()).isEqualTo(REFERENCE_TYPE_ID);
        verify(masterDataFieldRepository).findById(1L);
    }

    @Test
    void findById_notFound() {
        when(masterDataFieldRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterDataFieldService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(MasterDataFieldErrorCode.MASTER_DATA_FIELD_NOT_FOUND);

        verify(masterDataFieldRepository).findById(99L);
    }

    @Test
    void listAll() {
        MasterDataFieldEntity entity = mock(MasterDataFieldEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(entity.getFieldName()).thenReturn(FIELD_CODE);
        when(entity.getName()).thenReturn(FIELD_NAME);
        when(entity.getDataType()).thenReturn(MasterDataFieldDataType.STRING);
        when(entity.isRequired()).thenReturn(true);
        when(entity.getDisplayOrder()).thenReturn(1);
        when(entity.getDescription()).thenReturn(null);
        when(entity.getDefaultValue()).thenReturn(null);
        when(entity.isUnique()).thenReturn(false);
        when(entity.isSearchable()).thenReturn(false);
        when(entity.getMasterDataReferenceTypeId()).thenReturn(null);
        Page<MasterDataFieldEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);
        when(masterDataFieldRepository.findByWorkspaceId(eq(WORKSPACE_ID), eq(null), any(Pageable.class)))
                .thenReturn(page);
        MasterDataTypeEntity type = mock(MasterDataTypeEntity.class);
        when(type.getId()).thenReturn(TYPE_ID);
        when(type.getCode()).thenReturn(TYPE_CODE);
        when(type.getName()).thenReturn(TYPE_NAME);
        when(masterDataTypeRepository.findAllById(List.of(TYPE_ID))).thenReturn(List.of(type));

        PageResult<MasterDataFieldListResponse> result = masterDataFieldService.findAll(null, 1, 20, null);

        assertThat(result.content()).hasSize(1);
        MasterDataFieldListResponse item = result.content().get(0);
        assertThat(item.typeId()).isEqualTo(TYPE_ID);
        assertThat(item.typeCode()).isEqualTo(TYPE_CODE);
        assertThat(item.typeName()).isEqualTo(TYPE_NAME);
        assertThat(item.code()).isEqualTo(FIELD_CODE);
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.meta().totalRecords()).isEqualTo(1);
        verify(masterDataFieldRepository).findByWorkspaceId(eq(WORKSPACE_ID), eq(null), any(Pageable.class));
        verify(masterDataFieldRepository, never()).findByMasterDataTypeId(eq(TYPE_ID), any(Pageable.class));
    }

    @Test
    void listByType() {
        stubTypeInWorkspace(TYPE_ID);
        MasterDataFieldEntity entity = mock(MasterDataFieldEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(entity.getFieldName()).thenReturn(FIELD_CODE);
        when(entity.getName()).thenReturn(FIELD_NAME);
        when(entity.getDataType()).thenReturn(MasterDataFieldDataType.STRING);
        when(entity.isRequired()).thenReturn(true);
        when(entity.getDisplayOrder()).thenReturn(1);
        when(entity.getDescription()).thenReturn(null);
        when(entity.getDefaultValue()).thenReturn(null);
        when(entity.isUnique()).thenReturn(false);
        when(entity.isSearchable()).thenReturn(false);
        when(entity.getMasterDataReferenceTypeId()).thenReturn(null);
        Page<MasterDataFieldEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);
        when(masterDataFieldRepository.findByMasterDataTypeId(eq(TYPE_ID), any(Pageable.class)))
                .thenReturn(page);
        MasterDataTypeEntity type = mock(MasterDataTypeEntity.class);
        when(type.getId()).thenReturn(TYPE_ID);
        when(type.getCode()).thenReturn(TYPE_CODE);
        when(type.getName()).thenReturn(TYPE_NAME);
        when(masterDataTypeRepository.findAllById(List.of(TYPE_ID))).thenReturn(List.of(type));

        PageResult<MasterDataFieldListResponse> result = masterDataFieldService.findAll(TYPE_ID, 1, 20, null);

        assertThat(result.content()).hasSize(1);
        MasterDataFieldListResponse item = result.content().get(0);
        assertThat(item.typeId()).isEqualTo(TYPE_ID);
        assertThat(item.typeCode()).isEqualTo(TYPE_CODE);
        assertThat(item.typeName()).isEqualTo(TYPE_NAME);
        assertThat(item.code()).isEqualTo(FIELD_CODE);
        assertThat(result.meta().pageSize()).isEqualTo(20);
        assertThat(result.meta().totalPages()).isEqualTo(1);
        verify(masterDataFieldRepository).findByMasterDataTypeId(eq(TYPE_ID), any(Pageable.class));
    }

    @Test
    void listAll_empty() {
        Page<MasterDataFieldEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(masterDataFieldRepository.findByWorkspaceId(eq(WORKSPACE_ID), eq(null), any(Pageable.class)))
                .thenReturn(page);

        PageResult<MasterDataFieldListResponse> result = masterDataFieldService.findAll(null, 1, 20, "");

        assertThat(result.content()).isEmpty();
        assertThat(result.meta().totalRecords()).isZero();
        assertThat(result.meta().totalPages()).isZero();
        verify(masterDataFieldRepository).findByWorkspaceId(eq(WORKSPACE_ID), eq(null), any(Pageable.class));
    }

    @Test
    void listAll_withKeyword() {
        Page<MasterDataFieldEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(masterDataFieldRepository.findByWorkspaceId(eq(WORKSPACE_ID), eq("Game"), any(Pageable.class)))
                .thenReturn(page);

        masterDataFieldService.findAll(null, 1, 20, "  Game  ");

        verify(masterDataFieldRepository).findByWorkspaceId(eq(WORKSPACE_ID), eq("Game"), any(Pageable.class));
        verify(masterDataFieldRepository, never()).findByWorkspaceId(eq(WORKSPACE_ID), eq(null), any(Pageable.class));
    }

    @Test
    void listByType_withKeyword() {
        stubTypeInWorkspace(TYPE_ID);
        Page<MasterDataFieldEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(masterDataFieldRepository.findByMasterDataTypeIdAndNameContainingIgnoreCase(
                        eq(TYPE_ID), eq("Game"), any(Pageable.class)))
                .thenReturn(page);

        masterDataFieldService.findAll(TYPE_ID, 1, 20, "  Game  ");

        verify(masterDataFieldRepository)
                .findByMasterDataTypeIdAndNameContainingIgnoreCase(eq(TYPE_ID), eq("Game"), any(Pageable.class));
        verify(masterDataFieldRepository, never()).findByMasterDataTypeId(eq(TYPE_ID), any(Pageable.class));
    }
}
