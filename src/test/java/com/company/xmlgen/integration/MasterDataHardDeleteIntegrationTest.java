package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataRecordRequest;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.entity.MasterDataFieldDataType;
import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;
import com.company.xmlgen.masterdata.repository.MasterDataFieldRepository;
import com.company.xmlgen.masterdata.repository.MasterDataRecordRepository;
import com.company.xmlgen.masterdata.repository.MasterDataTypeRepository;
import com.company.xmlgen.masterdata.service.MasterDataFieldService;
import com.company.xmlgen.masterdata.service.MasterDataRecordService;
import com.company.xmlgen.masterdata.service.MasterDataTypeService;
import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfig.class)
@EnabledIf("isDockerAvailable")
class MasterDataHardDeleteIntegrationTest {

    @Autowired
    private MasterDataTypeService masterDataTypeService;

    @Autowired
    private MasterDataFieldService masterDataFieldService;

    @Autowired
    private MasterDataRecordService masterDataRecordService;

    @Autowired
    private MasterDataTypeRepository masterDataTypeRepository;

    @Autowired
    private MasterDataFieldRepository masterDataFieldRepository;

    @Autowired
    private MasterDataRecordRepository masterDataRecordRepository;

    @BeforeEach
    void setUp() {
        AuthenticatedUser currentUser = new AuthenticatedUser(1L, "admin", true);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null, null));
        WorkspaceTestSupport.useDefaultWorkspace();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        WorkspaceTestSupport.clearWorkspace();
    }

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Test
    void createAfterDelete_allowsSameMasterDataTypeCode() {
        CreateMasterDataTypeRequest request = typeRequest(uniqueCode("MDT_DELETE"));

        Long id = masterDataTypeService.create(request).id();
        masterDataTypeService.delete(id);

        Long recreatedId = masterDataTypeService.create(request).id();

        assertThat(recreatedId).isNotEqualTo(id);
        assertThat(masterDataTypeRepository.findById(id)).isEmpty();
        assertThat(masterDataTypeRepository.findById(recreatedId)).isPresent();
    }

    @Test
    void deleteParent_cascadesToFieldsAndRecords() {
        Long typeId = masterDataTypeService.create(typeRequest(uniqueCode("MDT_CASCADE"))).id();
        masterDataFieldService.create(uniqueFieldRequest(typeId, "game_kind_id", 1));
        masterDataRecordService.create(new CreateMasterDataRecordRequest(typeId, recordData(1)));

        masterDataTypeService.delete(typeId);

        assertThat(masterDataTypeRepository.findById(typeId)).isEmpty();
        assertThat(masterDataFieldRepository.findAllByMasterDataTypeId(typeId)).isEmpty();
        assertThat(masterDataRecordRepository.search(typeId, null, PageRequest.of(0, 20)).getContent())
                .isEmpty();
    }

    @Test
    void deleteReferencedType_setsReferenceFieldToNull() {
        Long ownerTypeId = masterDataTypeService.create(typeRequest(uniqueCode("MDT_OWNER"))).id();
        Long referencedTypeId = masterDataTypeService.create(typeRequest(uniqueCode("MDT_REFERENCED"))).id();
        Long fieldId = masterDataFieldService
                .create(referenceFieldRequest(ownerTypeId, "referenced_type_id", 1, referencedTypeId))
                .id();

        masterDataTypeService.delete(referencedTypeId);

        assertThat(masterDataTypeRepository.findById(referencedTypeId)).isEmpty();
        assertThat(masterDataFieldRepository.findById(fieldId))
                .isPresent()
                .get()
                .extracting(field -> field.getMasterDataReferenceTypeId())
                .isNull();
        assertThat(masterDataFieldRepository.findAllByMasterDataTypeId(ownerTypeId)).hasSize(1);
    }

    @Test
    void createAfterDelete_allowsSameMasterDataFieldCode() {
        Long typeId = masterDataTypeService.create(typeRequest(uniqueCode("MDF_DELETE"))).id();
        CreateMasterDataFieldRequest request = uniqueFieldRequest(typeId, "game_kind_id", 1);

        Long id = masterDataFieldService.create(request).id();
        masterDataFieldService.delete(id);

        Long recreatedId = masterDataFieldService.create(request).id();

        assertThat(recreatedId).isNotEqualTo(id);
        assertThat(masterDataFieldRepository.findById(id)).isEmpty();
        assertThat(masterDataFieldRepository.findById(recreatedId)).isPresent();
    }

    @Test
    void createAfterDelete_allowsSameUniqueMasterDataRecordValue() {
        Long typeId = masterDataTypeService.create(typeRequest(uniqueCode("MDR_DELETE"))).id();
        masterDataFieldService.create(uniqueFieldRequest(typeId, "game_kind_id", 1));
        CreateMasterDataRecordRequest request = new CreateMasterDataRecordRequest(typeId, recordData(1));

        Long id = masterDataRecordService.create(request).id();
        masterDataRecordService.delete(id);

        Long recreatedId = masterDataRecordService.create(request).id();

        assertThat(recreatedId).isNotEqualTo(id);
        assertThat(masterDataRecordRepository.findById(id)).isEmpty();
        assertThat(masterDataRecordRepository.findById(recreatedId)).isPresent();
    }

    private static CreateMasterDataTypeRequest typeRequest(String code) {
        return new CreateMasterDataTypeRequest(code, code + " Name", "description", MasterDataTypeStatus.ACTIVE);
    }

    private static CreateMasterDataFieldRequest uniqueFieldRequest(Long typeId, String code, int displayOrder) {
        return new CreateMasterDataFieldRequest(
                typeId,
                code,
                code + " name",
                MasterDataFieldDataType.INTEGER,
                true,
                displayOrder,
                null,
                null,
                true,
                true,
                null);
    }

    private static CreateMasterDataFieldRequest referenceFieldRequest(
            Long typeId, String code, int displayOrder, Long referenceTypeId) {
        return new CreateMasterDataFieldRequest(
                typeId,
                code,
                code + " name",
                MasterDataFieldDataType.INTEGER,
                false,
                displayOrder,
                null,
                null,
                false,
                true,
                referenceTypeId);
    }

    private static JsonNode recordData(int value) {
        return JsonNodeFactory.instance.objectNode().put("game_kind_id", value);
    }

    private static String uniqueCode(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
