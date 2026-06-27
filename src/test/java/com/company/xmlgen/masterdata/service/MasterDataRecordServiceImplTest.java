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
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.request.UpdateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordDetailResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordListResponse;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordResponse;
import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
class MasterDataRecordServiceImplTest {

    private static final Long TYPE_ID = 3L;

    @Mock
    private MasterDataRecordRepository masterDataRecordRepository;

    @Mock
    private MasterDataTypeRepository masterDataTypeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MasterDataRecordServiceImpl masterDataRecordService;

    @BeforeEach
    void setUp() {
        masterDataRecordService =
                new MasterDataRecordServiceImpl(masterDataRecordRepository, masterDataTypeRepository, objectMapper);
    }

    @Test
    void create_success() {
        JsonNode data = JsonNodeFactory.instance
                .objectNode()
                .put("game_kind_id", 1)
                .put("game_kind_name", "J1");
        CreateMasterDataRecordRequest request = new CreateMasterDataRecordRequest(TYPE_ID, data);
        when(masterDataTypeRepository.existsById(TYPE_ID)).thenReturn(true);
        MasterDataRecordEntity persisted = mock(MasterDataRecordEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(persisted.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(persisted.getDataJson()).thenReturn(data);
        when(masterDataRecordRepository.save(any(MasterDataRecordEntity.class))).thenReturn(persisted);

        MasterDataRecordResponse response = masterDataRecordService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.typeId()).isEqualTo(TYPE_ID);
        assertThat(response.data()).isEqualTo(data);

        verify(masterDataTypeRepository).existsById(TYPE_ID);
        ArgumentCaptor<MasterDataRecordEntity> captor = ArgumentCaptor.forClass(MasterDataRecordEntity.class);
        verify(masterDataRecordRepository).save(captor.capture());
        MasterDataRecordEntity saved = captor.getValue();
        assertThat(saved.getMasterDataTypeId()).isEqualTo(TYPE_ID);
        assertThat(saved.getDataJson()).isEqualTo(data);
        assertThat(saved.getDeletedAt()).isNull();
    }

    @Test
    void create_typeNotFound() {
        JsonNode data = JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1");
        CreateMasterDataRecordRequest request = new CreateMasterDataRecordRequest(99L, data);
        when(masterDataTypeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> masterDataRecordService.create(request))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(MasterDataTypeErrorCode.MASTER_DATA_TYPE_NOT_FOUND);

        verify(masterDataTypeRepository).existsById(99L);
        verify(masterDataRecordRepository, never()).save(any());
    }

    @Test
    void findById_success() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
        JsonNode data = JsonNodeFactory.instance
                .objectNode()
                .put("game_kind_id", 1)
                .put("game_kind_name", "J1");
        MasterDataRecordEntity entity = mock(MasterDataRecordEntity.class);
        when(entity.getId()).thenReturn(10L);
        when(entity.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(entity.getDataJson()).thenReturn(data);
        when(entity.getCreatedAt()).thenReturn(createdAt);
        when(entity.getUpdatedAt()).thenReturn(updatedAt);
        when(entity.getDeletedAt()).thenReturn(null);
        when(masterDataRecordRepository.findById(10L)).thenReturn(Optional.of(entity));

        MasterDataRecordDetailResponse response = masterDataRecordService.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.typeId()).isEqualTo(TYPE_ID);
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
        verify(masterDataRecordRepository).findById(10L);
    }

    @Test
    void findById_notFound() {
        when(masterDataRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterDataRecordService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode().code())
                .isEqualTo("MASTER_DATA_RECORD_NOT_FOUND");

        verify(masterDataRecordRepository).findById(99L);
    }

    @Test
    void update_success() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-03T00:00:00Z");
        JsonNode oldData = JsonNodeFactory.instance
                .objectNode()
                .put("game_kind_id", 1)
                .put("game_kind_name", "J1");
        JsonNode newData = JsonNodeFactory.instance
                .objectNode()
                .put("game_kind_id", 2)
                .put("game_kind_name", "J2");
        MasterDataRecordEntity entity = new MasterDataRecordEntity(TYPE_ID, oldData);
        MasterDataRecordEntity saved = mock(MasterDataRecordEntity.class);
        when(saved.getId()).thenReturn(10L);
        when(saved.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(saved.getDataJson()).thenReturn(newData);
        when(saved.getCreatedAt()).thenReturn(createdAt);
        when(saved.getUpdatedAt()).thenReturn(updatedAt);
        when(masterDataRecordRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(masterDataRecordRepository.save(entity)).thenReturn(saved);

        MasterDataRecordDetailResponse response =
                masterDataRecordService.update(10L, new UpdateMasterDataRecordRequest(newData));

        assertThat(entity.getMasterDataTypeId()).isEqualTo(TYPE_ID);
        assertThat(entity.getDataJson()).isEqualTo(newData);
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.typeId()).isEqualTo(TYPE_ID);
        assertThat(response.data()).isEqualTo(newData);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
        verify(masterDataRecordRepository).findById(10L);
        verify(masterDataRecordRepository).save(entity);
    }

    @Test
    void update_notFound() {
        JsonNode data = JsonNodeFactory.instance.objectNode().put("game_kind_name", "J2");
        when(masterDataRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterDataRecordService.update(99L, new UpdateMasterDataRecordRequest(data)))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode().code())
                .isEqualTo("MASTER_DATA_RECORD_NOT_FOUND");

        verify(masterDataRecordRepository).findById(99L);
        verify(masterDataRecordRepository, never()).save(any());
    }

    @Test
    void update_replaceEntireJson() {
        JsonNode oldData = JsonNodeFactory.instance
                .objectNode()
                .put("game_kind_id", 1)
                .put("game_kind_name", "J1")
                .put("extra_field", "remove me");
        JsonNode newData = JsonNodeFactory.instance
                .objectNode()
                .put("game_kind_id", 2)
                .put("game_kind_name", "J2");
        MasterDataRecordEntity entity = new MasterDataRecordEntity(TYPE_ID, oldData);
        when(masterDataRecordRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(masterDataRecordRepository.save(entity)).thenReturn(entity);

        masterDataRecordService.update(10L, new UpdateMasterDataRecordRequest(newData));

        assertThat(entity.getMasterDataTypeId()).isEqualTo(TYPE_ID);
        assertThat(entity.getDataJson()).isEqualTo(newData);
        assertThat(entity.getDataJson().has("extra_field")).isFalse();
        verify(masterDataRecordRepository).save(entity);
    }

    @Test
    void delete_success() {
        JsonNode data = JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1");
        MasterDataRecordEntity entity = new MasterDataRecordEntity(TYPE_ID, data);
        when(masterDataRecordRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(masterDataRecordRepository.save(entity)).thenReturn(entity);

        masterDataRecordService.delete(10L);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(masterDataRecordRepository).findById(10L);
        verify(masterDataRecordRepository, times(1)).save(entity);

        assertThatThrownBy(() -> masterDataRecordService.findById(10L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode().code())
                .isEqualTo("MASTER_DATA_RECORD_NOT_FOUND");
    }

    @Test
    void delete_notFound() {
        when(masterDataRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> masterDataRecordService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode().code())
                .isEqualTo("MASTER_DATA_RECORD_NOT_FOUND");

        verify(masterDataRecordRepository).findById(99L);
        verify(masterDataRecordRepository, never()).save(any());
    }

    @Test
    void list_success() {
        MasterDataRecordEntity entity = mock(MasterDataRecordEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(entity.getDataJson())
                .thenReturn(JsonNodeFactory.instance
                        .objectNode()
                        .put("game_kind_id", 1)
                        .put("game_kind_name", "J1"));
        Page<MasterDataRecordEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);
        when(masterDataRecordRepository.search(eq(TYPE_ID), eq(null), any(Pageable.class)))
                .thenReturn(page);

        PageResult<MasterDataRecordListResponse> result = masterDataRecordService.findAll(TYPE_ID, 1, 20, null);

        assertThat(result.content()).hasSize(1);
        MasterDataRecordListResponse item = result.content().get(0);
        assertThat(item.id()).isEqualTo(1L);
        assertThat(item.typeId()).isEqualTo(TYPE_ID);
        assertThat(item.values()).containsEntry("game_kind_id", 1).containsEntry("game_kind_name", "J1");
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.meta().pageSize()).isEqualTo(20);
        assertThat(result.meta().totalRecords()).isEqualTo(1);
        assertThat(result.meta().totalPages()).isEqualTo(1);
        verify(masterDataRecordRepository).search(eq(TYPE_ID), eq(null), any(Pageable.class));
    }

    @Test
    void list_empty() {
        Page<MasterDataRecordEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(masterDataRecordRepository.search(eq(TYPE_ID), eq(null), any(Pageable.class)))
                .thenReturn(page);

        PageResult<MasterDataRecordListResponse> result = masterDataRecordService.findAll(TYPE_ID, 1, 20, "");

        assertThat(result.content()).isEmpty();
        assertThat(result.meta().totalRecords()).isZero();
        assertThat(result.meta().totalPages()).isZero();
        verify(masterDataRecordRepository).search(eq(TYPE_ID), eq(null), any(Pageable.class));
    }

    @Test
    void list_keyword() {
        Page<MasterDataRecordEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(masterDataRecordRepository.search(eq(TYPE_ID), eq("J1"), any(Pageable.class)))
                .thenReturn(page);

        masterDataRecordService.findAll(TYPE_ID, 1, 20, "  J1  ");

        verify(masterDataRecordRepository).search(eq(TYPE_ID), eq("J1"), any(Pageable.class));
    }
}
