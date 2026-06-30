package com.company.xmlgen.xmlgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.xmlgeneration.exception.XMLGenerationErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SelectedMasterDataLoaderImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private MasterDataRecordRepository masterDataRecordRepository;

    @Mock
    private MasterDataTypeRepository masterDataTypeRepository;

    private SelectedMasterDataLoader loader;

    @BeforeEach
    void setUp() {
        loader = new SelectedMasterDataLoaderImpl(
                masterDataRecordRepository, masterDataTypeRepository, OBJECT_MAPPER);
    }

    @Test
    void load_recordReference_expandsToRecordDataJson() throws Exception {
        MasterDataTypeEntity type = mock(MasterDataTypeEntity.class);
        when(type.getCode()).thenReturn("GAME_KIND_ID");

        MasterDataRecordEntity record = mock(MasterDataRecordEntity.class);
        when(record.getMasterDataTypeId()).thenReturn(5L);
        when(record.getDeletedAt()).thenReturn(null);
        when(record.getDataJson()).thenReturn(OBJECT_MAPPER.readTree(
                """
                {
                  "game_kind_id": 68,
                  "game_kind_name": "明治安田Ｊ１リーグ"
                }
                """));

        when(masterDataRecordRepository.findById(11L)).thenReturn(Optional.of(record));
        when(masterDataTypeRepository.findById(5L)).thenReturn(Optional.of(type));

        var resolved = loader.load(OBJECT_MAPPER.readTree(
                """
                {
                  "GAME_KIND_ID": { "id": 11 }
                }
                """));

        assertThat(resolved.get("GAME_KIND_ID").get("game_kind_id").asInt()).isEqualTo(68);
        assertThat(resolved.get("GAME_KIND_ID").get("game_kind_name").asText()).isEqualTo("明治安田Ｊ１リーグ");
    }

    @Test
    void load_alreadyExpandedSelection_passesThrough() throws Exception {
        var resolved = loader.load(OBJECT_MAPPER.readTree(
                """
                {
                  "GAME_KIND": {
                    "game_kind_id": 2
                  }
                }
                """));

        assertThat(resolved.get("GAME_KIND").get("game_kind_id").asInt()).isEqualTo(2);
    }

    @Test
    void load_missingRecord_throwsNotFoundException() throws Exception {
        when(masterDataRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loader.load(OBJECT_MAPPER.readTree(
                        """
                        {
                          "GAME_KIND_ID": { "id": 99 }
                        }
                        """)))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(XMLGenerationErrorCode.MASTER_DATA_NOT_FOUND);
    }

    @Test
    void load_typeCodeMismatch_throwsBusinessException() throws Exception {
        MasterDataTypeEntity type = mock(MasterDataTypeEntity.class);
        when(type.getCode()).thenReturn("OTHER_TYPE");

        MasterDataRecordEntity record = mock(MasterDataRecordEntity.class);
        when(record.getMasterDataTypeId()).thenReturn(5L);
        when(record.getDeletedAt()).thenReturn(null);

        when(masterDataRecordRepository.findById(11L)).thenReturn(Optional.of(record));
        when(masterDataTypeRepository.findById(5L)).thenReturn(Optional.of(type));

        assertThatThrownBy(() -> loader.load(OBJECT_MAPPER.readTree(
                        """
                        {
                          "GAME_KIND_ID": { "id": 11 }
                        }
                        """)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(XMLGenerationErrorCode.MASTER_DATA_TYPE_MISMATCH);
    }
}
