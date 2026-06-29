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
import com.company.xmlgen.masterdata.entity.MasterDataFieldDataType;
import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import com.company.xmlgen.masterdata.exception.MasterDataTypeErrorCode;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.Instant;
import java.util.ArrayList;
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

    @Mock
    private MasterDataFieldRepository masterDataFieldRepository;

    @Mock
    private MasterDataValidationService masterDataValidationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MasterDataRecordServiceImpl masterDataRecordService;

    @BeforeEach
    void setUp() {
        masterDataRecordService =
                new MasterDataRecordServiceImpl(
                        masterDataRecordRepository,
                        masterDataTypeRepository,
                        masterDataValidationService,
                        objectMapper);
    }

    @Test
    void validate_returnsValid() {
        MasterDataValidationService validationService = validationService();
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1"));
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID)).thenReturn(List.of());

        ValidationResult result = validationService.validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void validationRules_executeInPriorityOrder() {
        List<String> executedRules = new ArrayList<>();
        MasterDataValidationService validationService = new MasterDataValidationServiceImpl(
                masterDataFieldRepository,
                List.of(
                        recordingRule("reference", 400, executedRules),
                        recordingRule("required", 100, executedRules),
                        recordingRule("unique", 300, executedRules),
                        recordingRule("dataType", 200, executedRules)));
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID)).thenReturn(List.of());

        validationService.validate(
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1")));

        assertThat(executedRules).containsExactly("required", "dataType", "unique", "reference");
    }

    @Test
    void required_success() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(requiredField("game_kind_name")));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1"));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void required_missingField() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(requiredField("game_kind_name")));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_id", 1));

        ValidationResult result = validationService().validate(context);

        assertRequiredFieldViolation(result, "game_kind_name");
    }

    @Test
    void required_null() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(requiredField("game_kind_name")));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().putNull("game_kind_name"));

        ValidationResult result = validationService().validate(context);

        assertRequiredFieldViolation(result, "game_kind_name");
    }

    @Test
    void required_blank() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(requiredField("game_kind_name")));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_name", "   "));

        ValidationResult result = validationService().validate(context);

        assertRequiredFieldViolation(result, "game_kind_name");
    }

    @Test
    void required_multipleErrors() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(requiredField("game_kind_id"), requiredField("game_kind_name")));
        ValidationContext context = ValidationContext.create(
                TYPE_ID,
                JsonNodeFactory.instance
                        .objectNode()
                        .putNull("game_kind_id")
                        .put("game_kind_name", ""));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(2);
        assertThat(result.errors().get(0).field()).isEqualTo("game_kind_id");
        assertThat(result.errors().get(0).code()).isEqualTo("REQUIRED_FIELD_MISSING");
        assertThat(result.errors().get(0).message()).isEqualTo("Field is required.");
        assertThat(result.errors().get(1).field()).isEqualTo("game_kind_name");
        assertThat(result.errors().get(1).code()).isEqualTo("REQUIRED_FIELD_MISSING");
        assertThat(result.errors().get(1).message()).isEqualTo("Field is required.");
    }

    @Test
    void string_success() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(field("game_kind_name", MasterDataFieldDataType.STRING)));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1"));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void integer_success() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(field("game_kind_id", MasterDataFieldDataType.INTEGER)));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_id", 1));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void integer_invalid() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(field("game_kind_id", MasterDataFieldDataType.INTEGER)));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_id", 1.5));

        ValidationResult result = validationService().validate(context);

        assertInvalidDataType(result, "game_kind_id", "Expected INTEGER.");
    }

    @Test
    void boolean_invalid() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(field("active", MasterDataFieldDataType.BOOLEAN)));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("active", "true"));

        ValidationResult result = validationService().validate(context);

        assertInvalidDataType(result, "active", "Expected BOOLEAN.");
    }

    @Test
    void date_invalid() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(field("effective_date", MasterDataFieldDataType.DATE)));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("effective_date", "2026/01/01"));

        ValidationResult result = validationService().validate(context);

        assertInvalidDataType(result, "effective_date", "Expected DATE.");
    }

    @Test
    void datetime_invalid() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(field("updated_time", MasterDataFieldDataType.DATETIME)));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("updated_time", "2026-01-01"));

        ValidationResult result = validationService().validate(context);

        assertInvalidDataType(result, "updated_time", "Expected DATETIME.");
    }

    @Test
    void multipleTypeErrors() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(
                        field("game_kind_id", MasterDataFieldDataType.INTEGER),
                        field("active", MasterDataFieldDataType.BOOLEAN),
                        field("effective_date", MasterDataFieldDataType.DATE)));
        ValidationContext context = ValidationContext.create(
                TYPE_ID,
                JsonNodeFactory.instance
                        .objectNode()
                        .put("game_kind_id", "1")
                        .put("active", "true")
                        .put("effective_date", "2026/01/01"));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(3);
        assertThat(result.errors().get(0).field()).isEqualTo("game_kind_id");
        assertThat(result.errors().get(0).code()).isEqualTo("INVALID_DATA_TYPE");
        assertThat(result.errors().get(0).message()).isEqualTo("Expected INTEGER.");
        assertThat(result.errors().get(1).field()).isEqualTo("active");
        assertThat(result.errors().get(1).code()).isEqualTo("INVALID_DATA_TYPE");
        assertThat(result.errors().get(1).message()).isEqualTo("Expected BOOLEAN.");
        assertThat(result.errors().get(2).field()).isEqualTo("effective_date");
        assertThat(result.errors().get(2).code()).isEqualTo("INVALID_DATA_TYPE");
        assertThat(result.errors().get(2).message()).isEqualTo("Expected DATE.");
    }

    @Test
    void unique_success() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(uniqueField("game_kind_id", MasterDataFieldDataType.INTEGER)));
        when(masterDataRecordRepository.existsDuplicateValue(TYPE_ID, "game_kind_id", "1", null))
                .thenReturn(false);
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_id", 1));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
        verify(masterDataRecordRepository).existsDuplicateValue(TYPE_ID, "game_kind_id", "1", null);
    }

    @Test
    void unique_duplicate() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(uniqueField("game_kind_id", MasterDataFieldDataType.INTEGER)));
        when(masterDataRecordRepository.existsDuplicateValue(TYPE_ID, "game_kind_id", "1", null))
                .thenReturn(true);
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_id", 1));

        ValidationResult result = validationService().validate(context);

        assertDuplicateValue(result, "game_kind_id");
    }

    @Test
    void unique_update_sameRecord() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(uniqueField("game_kind_id", MasterDataFieldDataType.INTEGER)));
        when(masterDataRecordRepository.existsDuplicateValue(TYPE_ID, "game_kind_id", "1", 10L))
                .thenReturn(false);
        ValidationContext context =
                ValidationContext.update(10L, TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_id", 1));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
        verify(masterDataRecordRepository).existsDuplicateValue(TYPE_ID, "game_kind_id", "1", 10L);
    }

    @Test
    void unique_duplicateValidationAfterHardDelete() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(uniqueField("game_kind_id", MasterDataFieldDataType.INTEGER)));
        when(masterDataRecordRepository.existsDuplicateValue(TYPE_ID, "game_kind_id", "1", null))
                .thenReturn(false);
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_id", 1));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
        verify(masterDataRecordRepository).existsDuplicateValue(TYPE_ID, "game_kind_id", "1", null);
    }

    @Test
    void multipleUniqueViolations() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(
                        uniqueField("game_kind_id", MasterDataFieldDataType.INTEGER),
                        uniqueField("game_kind_name", MasterDataFieldDataType.STRING)));
        when(masterDataRecordRepository.existsDuplicateValue(TYPE_ID, "game_kind_id", "1", null))
                .thenReturn(true);
        when(masterDataRecordRepository.existsDuplicateValue(TYPE_ID, "game_kind_name", "\"J1\"", null))
                .thenReturn(true);
        ValidationContext context = ValidationContext.create(
                TYPE_ID,
                JsonNodeFactory.instance.objectNode().put("game_kind_id", 1).put("game_kind_name", "J1"));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(2);
        assertThat(result.errors().get(0).field()).isEqualTo("game_kind_id");
        assertThat(result.errors().get(0).code()).isEqualTo("DUPLICATE_VALUE");
        assertThat(result.errors().get(0).message()).isEqualTo("Value already exists.");
        assertThat(result.errors().get(1).field()).isEqualTo("game_kind_name");
        assertThat(result.errors().get(1).code()).isEqualTo("DUPLICATE_VALUE");
        assertThat(result.errors().get(1).message()).isEqualTo("Value already exists.");
    }

    @Test
    void reference_success() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(referenceField("stadium_id", 4L)));
        when(masterDataRecordRepository.existsByIdAndMasterDataTypeId(100L, 4L)).thenReturn(true);
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("stadium_id", 100));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
        verify(masterDataRecordRepository).existsByIdAndMasterDataTypeId(100L, 4L);
    }

    @Test
    void reference_not_found() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(referenceField("stadium_id", 4L)));
        when(masterDataRecordRepository.existsByIdAndMasterDataTypeId(100L, 4L)).thenReturn(false);
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("stadium_id", 100));

        ValidationResult result = validationService().validate(context);

        assertReferenceNotFound(result, "stadium_id");
    }

    @Test
    void optional_reference_absent() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(referenceField("stadium_id", 4L)));
        ValidationContext context =
                ValidationContext.create(TYPE_ID, JsonNodeFactory.instance.objectNode().put("game_kind_id", 1));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
        verify(masterDataRecordRepository, never()).existsByIdAndMasterDataTypeId(any(), any());
    }

    @Test
    void multiple_reference_errors() {
        when(masterDataFieldRepository.findAllByMasterDataTypeId(TYPE_ID))
                .thenReturn(List.of(referenceField("stadium_id", 4L), referenceField("league_id", 5L)));
        when(masterDataRecordRepository.existsByIdAndMasterDataTypeId(100L, 4L)).thenReturn(false);
        when(masterDataRecordRepository.existsByIdAndMasterDataTypeId(200L, 5L)).thenReturn(false);
        ValidationContext context = ValidationContext.create(
                TYPE_ID,
                JsonNodeFactory.instance.objectNode().put("stadium_id", 100).put("league_id", 200));

        ValidationResult result = validationService().validate(context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(2);
        assertThat(result.errors().get(0).field()).isEqualTo("stadium_id");
        assertThat(result.errors().get(0).code()).isEqualTo("REFERENCE_NOT_FOUND");
        assertThat(result.errors().get(0).message()).isEqualTo("Referenced master data record does not exist.");
        assertThat(result.errors().get(1).field()).isEqualTo("league_id");
        assertThat(result.errors().get(1).code()).isEqualTo("REFERENCE_NOT_FOUND");
        assertThat(result.errors().get(1).message()).isEqualTo("Referenced master data record does not exist.");
    }

    @Test
    void create_success() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-01T00:00:00Z");
        JsonNode data = JsonNodeFactory.instance
                .objectNode()
                .put("game_kind_id", 1)
                .put("game_kind_name", "J1");
        CreateMasterDataRecordRequest request = new CreateMasterDataRecordRequest(TYPE_ID, data);
        when(masterDataTypeRepository.existsById(TYPE_ID)).thenReturn(true);
        when(masterDataValidationService.validate(any(ValidationContext.class))).thenReturn(ValidationResult.valid());
        MasterDataRecordEntity persisted = mock(MasterDataRecordEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(persisted.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(persisted.getDataJson()).thenReturn(data);
        when(persisted.getCreatedAt()).thenReturn(createdAt);
        when(persisted.getUpdatedAt()).thenReturn(updatedAt);
        when(masterDataRecordRepository.save(any(MasterDataRecordEntity.class))).thenReturn(persisted);

        MasterDataRecordDetailResponse response = masterDataRecordService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.typeId()).isEqualTo(TYPE_ID);
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);

        verify(masterDataTypeRepository).existsById(TYPE_ID);
        verify(masterDataValidationService).validate(any(ValidationContext.class));
        ArgumentCaptor<MasterDataRecordEntity> captor = ArgumentCaptor.forClass(MasterDataRecordEntity.class);
        verify(masterDataRecordRepository).save(captor.capture());
        MasterDataRecordEntity saved = captor.getValue();
        assertThat(saved.getMasterDataTypeId()).isEqualTo(TYPE_ID);
        assertThat(saved.getDataJson()).isEqualTo(data);
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
    void create_callsValidationService() {
        JsonNode data = JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1");
        CreateMasterDataRecordRequest request = new CreateMasterDataRecordRequest(TYPE_ID, data);
        when(masterDataTypeRepository.existsById(TYPE_ID)).thenReturn(true);
        when(masterDataValidationService.validate(any(ValidationContext.class))).thenReturn(ValidationResult.valid());
        MasterDataRecordEntity persisted = mock(MasterDataRecordEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(persisted.getMasterDataTypeId()).thenReturn(TYPE_ID);
        when(persisted.getDataJson()).thenReturn(data);
        when(masterDataRecordRepository.save(any(MasterDataRecordEntity.class))).thenReturn(persisted);

        masterDataRecordService.create(request);

        ArgumentCaptor<ValidationContext> captor = ArgumentCaptor.forClass(ValidationContext.class);
        verify(masterDataValidationService, times(1)).validate(captor.capture());
        assertThat(captor.getValue().typeId()).isEqualTo(TYPE_ID);
        assertThat(captor.getValue().data()).isEqualTo(data);
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
        when(masterDataValidationService.validate(any(ValidationContext.class))).thenReturn(ValidationResult.valid());
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
        verify(masterDataValidationService).validate(any(ValidationContext.class));
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
        when(masterDataValidationService.validate(any(ValidationContext.class))).thenReturn(ValidationResult.valid());
        when(masterDataRecordRepository.save(entity)).thenReturn(entity);

        masterDataRecordService.update(10L, new UpdateMasterDataRecordRequest(newData));

        assertThat(entity.getMasterDataTypeId()).isEqualTo(TYPE_ID);
        assertThat(entity.getDataJson()).isEqualTo(newData);
        assertThat(entity.getDataJson().has("extra_field")).isFalse();
        verify(masterDataRecordRepository).save(entity);
    }

    @Test
    void update_callsValidationService() {
        JsonNode oldData = JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1");
        JsonNode newData = JsonNodeFactory.instance.objectNode().put("game_kind_name", "J2");
        MasterDataRecordEntity entity = new MasterDataRecordEntity(TYPE_ID, oldData);
        when(masterDataRecordRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(masterDataValidationService.validate(any(ValidationContext.class))).thenReturn(ValidationResult.valid());
        when(masterDataRecordRepository.save(entity)).thenReturn(entity);

        masterDataRecordService.update(10L, new UpdateMasterDataRecordRequest(newData));

        ArgumentCaptor<ValidationContext> captor = ArgumentCaptor.forClass(ValidationContext.class);
        verify(masterDataValidationService, times(1)).validate(captor.capture());
        assertThat(captor.getValue().typeId()).isEqualTo(TYPE_ID);
        assertThat(captor.getValue().data()).isEqualTo(newData);
    }

    @Test
    void delete_success() {
        JsonNode data = JsonNodeFactory.instance.objectNode().put("game_kind_name", "J1");
        MasterDataRecordEntity entity = new MasterDataRecordEntity(TYPE_ID, data);
        when(masterDataRecordRepository.findById(10L)).thenReturn(Optional.of(entity));

        masterDataRecordService.delete(10L);

        verify(masterDataRecordRepository).findById(10L);
        verify(masterDataRecordRepository, times(1)).delete(entity);
        verify(masterDataRecordRepository, never()).save(any());
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
        assertThat(item.data()).containsEntry("game_kind_id", 1).containsEntry("game_kind_name", "J1");
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

    private MasterDataValidationService validationService() {
        return new MasterDataValidationServiceImpl(
                masterDataFieldRepository,
                List.of(
                        new RequiredValidationRule(),
                        new DataTypeValidationRule(),
                        new UniqueValidationRule(masterDataRecordRepository),
                        new ReferenceValidationRule(masterDataRecordRepository)));
    }

    private static MasterDataFieldEntity requiredField(String fieldName) {
        return new MasterDataFieldEntity(TYPE_ID, fieldName, fieldName, MasterDataFieldDataType.STRING, true, 1);
    }

    private static MasterDataFieldEntity field(String fieldName, MasterDataFieldDataType dataType) {
        return new MasterDataFieldEntity(TYPE_ID, fieldName, fieldName, dataType, false, 1);
    }

    private static MasterDataFieldEntity uniqueField(String fieldName, MasterDataFieldDataType dataType) {
        MasterDataFieldEntity field = field(fieldName, dataType);
        field.setUnique(true);
        return field;
    }

    private static MasterDataFieldEntity referenceField(String fieldName, Long referenceTypeId) {
        MasterDataFieldEntity field = field(fieldName, MasterDataFieldDataType.INTEGER);
        field.setMasterDataReferenceTypeId(referenceTypeId);
        return field;
    }

    private static ValidationRule recordingRule(String name, int priority, List<String> executedRules) {
        return new ValidationRule() {
            @Override
            public int priority() {
                return priority;
            }

            @Override
            public List<ValidationError> validate(ValidationContext context) {
                executedRules.add(name);
                return List.of();
            }
        };
    }

    private static void assertRequiredFieldViolation(ValidationResult result, String fieldName) {
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(1);
        ValidationError error = result.errors().getFirst();
        assertThat(error.field()).isEqualTo(fieldName);
        assertThat(error.code()).isEqualTo("REQUIRED_FIELD_MISSING");
        assertThat(error.message()).isEqualTo("Field is required.");
    }

    private static void assertInvalidDataType(ValidationResult result, String fieldName, String message) {
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(1);
        ValidationError error = result.errors().getFirst();
        assertThat(error.field()).isEqualTo(fieldName);
        assertThat(error.code()).isEqualTo("INVALID_DATA_TYPE");
        assertThat(error.message()).isEqualTo(message);
    }

    private static void assertDuplicateValue(ValidationResult result, String fieldName) {
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(1);
        ValidationError error = result.errors().getFirst();
        assertThat(error.field()).isEqualTo(fieldName);
        assertThat(error.code()).isEqualTo("DUPLICATE_VALUE");
        assertThat(error.message()).isEqualTo("Value already exists.");
    }

    private static void assertReferenceNotFound(ValidationResult result, String fieldName) {
        assertThat(result.isValid()).isFalse();
        assertThat(result.errors()).hasSize(1);
        ValidationError error = result.errors().getFirst();
        assertThat(error.field()).isEqualTo(fieldName);
        assertThat(error.code()).isEqualTo("REFERENCE_NOT_FOUND");
        assertThat(error.message()).isEqualTo("Referenced master data record does not exist.");
    }
}
