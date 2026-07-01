package com.company.xmlgen.masterdata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.dto.response.CreateMasterDataTypeResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataTypeListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataTypeResponse;
import com.company.xmlgen.masterdata.dto.response.UpdateMasterDataTypeResponse;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;
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
class MasterDataTypeServiceImplTest {

    private static final Long WORKSPACE_ID = 1L;
    private static final String TYPE_CODE = "GAME_KIND";
    private static final String TYPE_NAME = "Game Kind";
    private static final String TYPE_DESCRIPTION = "Game kind master";

    @Mock
    private MasterDataTypeRepository masterDataTypeRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private MasterDataFieldRepository masterDataFieldRepository;

    @Mock
    private MasterDataRecordRepository masterDataRecordRepository;

    private MasterDataTypeServiceImpl masterDataTypeService;

    @BeforeEach
    void setUp() {
        WorkspaceOwnershipGuard workspaceOwnershipGuard = new WorkspaceOwnershipGuard(
                templateRepository,
                masterDataTypeRepository,
                masterDataFieldRepository,
                masterDataRecordRepository);
        masterDataTypeService = new MasterDataTypeServiceImpl(masterDataTypeRepository, workspaceOwnershipGuard);
        WorkspaceTestSupport.useDefaultWorkspace();
    }

    @AfterEach
    void tearDown() {
        WorkspaceTestSupport.clearWorkspace();
    }

    @Test
    void create_success() {
        CreateMasterDataTypeRequest request = new CreateMasterDataTypeRequest(
                TYPE_CODE, TYPE_NAME, TYPE_DESCRIPTION, MasterDataTypeStatus.ACTIVE);
        when(masterDataTypeRepository.existsByWorkspaceIdAndCode(WORKSPACE_ID, TYPE_CODE)).thenReturn(false);
        MasterDataTypeEntity persisted = mock(MasterDataTypeEntity.class);
        when(persisted.getId()).thenReturn(1L);
        when(persisted.getCode()).thenReturn(TYPE_CODE);
        when(persisted.getName()).thenReturn(TYPE_NAME);
        when(persisted.getDescription()).thenReturn(TYPE_DESCRIPTION);
        when(persisted.getStatus()).thenReturn(MasterDataTypeStatus.ACTIVE);
        when(masterDataTypeRepository.save(any(MasterDataTypeEntity.class))).thenReturn(persisted);

        CreateMasterDataTypeResponse response = masterDataTypeService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo(TYPE_CODE);
        assertThat(response.name()).isEqualTo(TYPE_NAME);
        assertThat(response.description()).isEqualTo(TYPE_DESCRIPTION);
        assertThat(response.status()).isEqualTo(MasterDataTypeStatus.ACTIVE);

        verify(masterDataTypeRepository).existsByWorkspaceIdAndCode(WORKSPACE_ID, TYPE_CODE);

        ArgumentCaptor<MasterDataTypeEntity> captor = ArgumentCaptor.forClass(MasterDataTypeEntity.class);
        verify(masterDataTypeRepository).save(captor.capture());
        MasterDataTypeEntity saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo(TYPE_CODE);
        assertThat(saved.getName()).isEqualTo(TYPE_NAME);
        assertThat(saved.getDescription()).isEqualTo(TYPE_DESCRIPTION);
        assertThat(saved.getStatus()).isEqualTo(MasterDataTypeStatus.ACTIVE);
        assertThat(saved.getWorkspaceId()).isEqualTo(WORKSPACE_ID);
    }

    @Test
    void create_duplicateCode() {
        CreateMasterDataTypeRequest request = new CreateMasterDataTypeRequest(
                TYPE_CODE, TYPE_NAME, TYPE_DESCRIPTION, MasterDataTypeStatus.ACTIVE);
        when(masterDataTypeRepository.existsByWorkspaceIdAndCode(WORKSPACE_ID, TYPE_CODE)).thenReturn(true);

        assertThatThrownBy(() -> masterDataTypeService.create(request))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(MasterDataTypeErrorCode.MASTER_DATA_TYPE_CODE_ALREADY_EXISTS);

        verify(masterDataTypeRepository).existsByWorkspaceIdAndCode(WORKSPACE_ID, TYPE_CODE);
        verify(masterDataTypeRepository, never()).save(any());
    }

    @Test
    void update_success() {
        MasterDataTypeEntity entity =
                new MasterDataTypeEntity(TYPE_CODE, TYPE_NAME, MasterDataTypeStatus.ACTIVE);
        entity.setDescription(TYPE_DESCRIPTION);
        entity.setWorkspaceId(WORKSPACE_ID);
        when(masterDataTypeRepository.findByIdAndWorkspaceId(1L, WORKSPACE_ID)).thenReturn(Optional.of(entity));
        when(masterDataTypeRepository.save(entity)).thenReturn(entity);

        UpdateMasterDataTypeRequest request = new UpdateMasterDataTypeRequest(
                "Game Kind Updated", "Updated description", MasterDataTypeStatus.INACTIVE);
        UpdateMasterDataTypeResponse response = masterDataTypeService.update(1L, request);

        assertThat(entity.getCode()).isEqualTo(TYPE_CODE);
        assertThat(entity.getName()).isEqualTo("Game Kind Updated");
        assertThat(entity.getDescription()).isEqualTo("Updated description");
        assertThat(entity.getStatus()).isEqualTo(MasterDataTypeStatus.INACTIVE);
        assertThat(response.code()).isEqualTo(TYPE_CODE);
        assertThat(response.name()).isEqualTo("Game Kind Updated");
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.status()).isEqualTo(MasterDataTypeStatus.INACTIVE);
        verify(masterDataTypeRepository).findByIdAndWorkspaceId(1L, WORKSPACE_ID);
        verify(masterDataTypeRepository).save(entity);
    }

    @Test
    void update_notFound() {
        when(masterDataTypeRepository.findByIdAndWorkspaceId(99L, WORKSPACE_ID)).thenReturn(Optional.empty());

        UpdateMasterDataTypeRequest request = new UpdateMasterDataTypeRequest(
                "Game Kind Updated", "Updated description", MasterDataTypeStatus.INACTIVE);
        assertThatThrownBy(() -> masterDataTypeService.update(99L, request))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND);

        verify(masterDataTypeRepository).findByIdAndWorkspaceId(99L, WORKSPACE_ID);
        verify(masterDataTypeRepository, never()).save(any());
    }

    @Test
    void delete_success() {
        MasterDataTypeEntity entity =
                new MasterDataTypeEntity(TYPE_CODE, TYPE_NAME, MasterDataTypeStatus.ACTIVE);
        entity.setWorkspaceId(WORKSPACE_ID);
        when(masterDataTypeRepository.findByIdAndWorkspaceId(1L, WORKSPACE_ID)).thenReturn(Optional.of(entity));

        masterDataTypeService.delete(1L);

        verify(masterDataTypeRepository).findByIdAndWorkspaceId(1L, WORKSPACE_ID);
        verify(masterDataTypeRepository).delete(entity);
        verify(masterDataTypeRepository, never()).save(any());
    }

    @Test
    void delete_notFound() {
        when(masterDataTypeRepository.findByIdAndWorkspaceId(99L, WORKSPACE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterDataTypeService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND);

        verify(masterDataTypeRepository).findByIdAndWorkspaceId(99L, WORKSPACE_ID);
        verify(masterDataTypeRepository, never()).delete(any());
        verify(masterDataTypeRepository, never()).save(any());
    }

    @Test
    void findById_success() {
        MasterDataTypeEntity entity = mock(MasterDataTypeEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getCode()).thenReturn(TYPE_CODE);
        when(entity.getName()).thenReturn(TYPE_NAME);
        when(entity.getStatus()).thenReturn(MasterDataTypeStatus.ACTIVE);
        when(masterDataTypeRepository.findByIdAndWorkspaceId(1L, WORKSPACE_ID)).thenReturn(Optional.of(entity));

        MasterDataTypeResponse response = masterDataTypeService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo(TYPE_CODE);
        assertThat(response.name()).isEqualTo(TYPE_NAME);
        assertThat(response.status()).isEqualTo(MasterDataTypeStatus.ACTIVE);
        verify(masterDataTypeRepository).findByIdAndWorkspaceId(1L, WORKSPACE_ID);
    }

    @Test
    void findById_notFound() {
        when(masterDataTypeRepository.findByIdAndWorkspaceId(99L, WORKSPACE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterDataTypeService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND);

        verify(masterDataTypeRepository).findByIdAndWorkspaceId(99L, WORKSPACE_ID);
    }

    @Test
    void findAll_success() {
        MasterDataTypeEntity entity = mock(MasterDataTypeEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getCode()).thenReturn(TYPE_CODE);
        when(entity.getName()).thenReturn(TYPE_NAME);
        when(entity.getStatus()).thenReturn(MasterDataTypeStatus.ACTIVE);
        Page<MasterDataTypeEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);
        when(masterDataTypeRepository.findByWorkspaceId(eq(WORKSPACE_ID), any(Pageable.class))).thenReturn(page);

        PageResult<MasterDataTypeListResponse> result = masterDataTypeService.findAll(1, 20, null);

        assertThat(result.content()).hasSize(1);
        MasterDataTypeListResponse item = result.content().get(0);
        assertThat(item.id()).isEqualTo(1L);
        assertThat(item.code()).isEqualTo(TYPE_CODE);
        assertThat(item.name()).isEqualTo(TYPE_NAME);
        assertThat(item.status()).isEqualTo(MasterDataTypeStatus.ACTIVE);
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.meta().pageSize()).isEqualTo(20);
        assertThat(result.meta().totalRecords()).isEqualTo(1);
        assertThat(result.meta().totalPages()).isEqualTo(1);
    }

    @Test
    void findAll_empty() {
        Page<MasterDataTypeEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(masterDataTypeRepository.findByWorkspaceId(eq(WORKSPACE_ID), any(Pageable.class))).thenReturn(page);

        PageResult<MasterDataTypeListResponse> result = masterDataTypeService.findAll(1, 20, "");

        assertThat(result.content()).isEmpty();
        assertThat(result.meta().totalRecords()).isZero();
        assertThat(result.meta().totalPages()).isZero();
    }

    @Test
    void findAll_keyword() {
        Page<MasterDataTypeEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(masterDataTypeRepository.findByWorkspaceIdAndNameContainingIgnoreCase(
                        eq(WORKSPACE_ID), eq("Game"), any(Pageable.class)))
                .thenReturn(page);

        masterDataTypeService.findAll(1, 20, "  Game  ");

        verify(masterDataTypeRepository)
                .findByWorkspaceIdAndNameContainingIgnoreCase(eq(WORKSPACE_ID), eq("Game"), any(Pageable.class));
        verify(masterDataTypeRepository, never()).findByWorkspaceId(any(), any());
    }
}
