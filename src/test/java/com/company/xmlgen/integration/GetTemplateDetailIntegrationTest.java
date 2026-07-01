package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataFieldRequest;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.entity.MasterDataFieldDataType;
import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;
import com.company.xmlgen.masterdata.service.MasterDataFieldService;
import com.company.xmlgen.masterdata.service.MasterDataTypeService;
import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.template.dto.request.CreateTemplateFieldRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateMappingRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.template.service.TemplateService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
class GetTemplateDetailIntegrationTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private MasterDataTypeService masterDataTypeService;

    @Autowired
    private MasterDataFieldService masterDataFieldService;

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
    void findById_withoutMetadata_returnsNullSchema() {
        String code = "GET_NO_METADATA_" + UUID.randomUUID();
        Long templateId = templateService
                .create(new CreateTemplateRequest(code, "Template", "description", null))
                .id();

        TemplateResponse response = templateService.findById(templateId);

        assertThat(response.schema()).isNull();
        assertThat(templateRepository.findById(templateId).orElseThrow().getCompiledSchemaJson())
                .isNull();
    }

    @Test
    void findById_withMetadata_reconstructsSchemaFromDatabase() {
        String code = "GET_WITH_METADATA_" + UUID.randomUUID();
        String typeCode = "MDT_" + UUID.randomUUID();
        Long masterDataFieldId = masterDataFieldService
                .create(new CreateMasterDataFieldRequest(
                        masterDataTypeService
                                .create(new CreateMasterDataTypeRequest(
                                        typeCode, "Game Kind", null, MasterDataTypeStatus.ACTIVE))
                                .id(),
                        "game_kind_id",
                        "Game Kind ID",
                        MasterDataFieldDataType.INTEGER,
                        true,
                        1,
                        null,
                        null,
                        true,
                        true,
                        null))
                .id();

        CreateTemplateSchemaRequest schema = new CreateTemplateSchemaRequest(
                List.of(
                        new CreateTemplateFieldRequest(
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
                                null),
                        new CreateTemplateFieldRequest(
                                "GameKindId",
                                "Game",
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
                                null)),
                List.of(new CreateTemplateMappingRequest("GameKindId", masterDataFieldId)));

        Long templateId = templateService
                .create(new CreateTemplateRequest(code, "Template", "description", schema))
                .id();

        TemplateResponse response = templateService.findById(templateId);

        assertThat(response.schema()).isNotNull();
        assertThat(response.schema().fields()).hasSize(2);
        assertThat(response.schema().fields().get(0).fieldName()).isEqualTo("Game");
        assertThat(response.schema().fields().get(0).parentFieldName()).isNull();
        assertThat(response.schema().fields().get(1).fieldName()).isEqualTo("GameKindId");
        assertThat(response.schema().fields().get(1).parentFieldName()).isEqualTo("Game");
        assertThat(response.schema().mappings()).hasSize(1);
        assertThat(response.schema().mappings().get(0).fieldName()).isEqualTo("GameKindId");
        assertThat(response.schema().mappings().get(0).masterDataFieldId()).isEqualTo(masterDataFieldId);
        assertThat(templateRepository.findById(templateId).orElseThrow().getCompiledSchemaJson())
                .isNotNull();
    }
}
