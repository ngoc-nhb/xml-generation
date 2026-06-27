package com.company.xmlgen.masterdata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.masterdata.dto.response.MasterDataRecordListResponse;
import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MasterDataRecordServiceImpl masterDataRecordService;

    @BeforeEach
    void setUp() {
        masterDataRecordService = new MasterDataRecordServiceImpl(masterDataRecordRepository, objectMapper);
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
