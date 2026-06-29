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
import com.company.xmlgen.template.dto.request.CreateTemplateFieldRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateMappingRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateSchemaRequest;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.repository.TemplateFieldRepository;
import com.company.xmlgen.template.repository.TemplateMappingRepository;
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
class CreateTemplateIntegrationTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private TemplateFieldRepository templateFieldRepository;

    @Autowired
    private TemplateMappingRepository templateMappingRepository;

    @Autowired
    private MasterDataTypeService masterDataTypeService;

    @Autowired
    private MasterDataFieldService masterDataFieldService;

    @BeforeEach
    void setUp() {
        AuthenticatedUser currentUser = new AuthenticatedUser(1L, "admin", true);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Test
    void create_withoutSchema_persistsTemplateOnly() {
        String code = "CREATE_NO_SCHEMA_" + UUID.randomUUID();

        Long templateId = templateService
                .create(new CreateTemplateRequest(code, "No Schema Template", "description", null))
                .id();

        var template = templateRepository.findById(templateId).orElseThrow();
        assertThat(template.getCompiledSchemaJson()).isNull();
        assertThat(templateFieldRepository.countByTemplateId(templateId)).isZero();
        assertThat(templateMappingRepository.countByTemplateId(templateId)).isZero();
    }

    @Test
    void create_withSchema_persistsMetadataAndCompiledSchema() {
        String code = "CREATE_WITH_SCHEMA_" + UUID.randomUUID();
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
        CreateTemplateFieldRequest mappedField = new CreateTemplateFieldRequest(
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
                null);
        CreateTemplateSchemaRequest schema = new CreateTemplateSchemaRequest(
                List.of(rootField, mappedField),
                List.of(new CreateTemplateMappingRequest("GameKindId", masterDataFieldId)));

        Long templateId = templateService
                .create(new CreateTemplateRequest(code, "Schema Template", "description", schema))
                .id();

        var template = templateRepository.findById(templateId).orElseThrow();
        assertThat(template.getCompiledSchemaJson()).isNotNull();
        assertThat(template.getCompiledSchemaJson().get("roots")).hasSize(2);
        assertThat(template.getCompiledSchemaJson().at("/roots/0/fieldName").asText()).isEqualTo("Game");
        assertThat(template.getCompiledSchemaJson().at("/mappings/0/fieldName").asText()).isEqualTo("GameKindId");
        assertThat(templateFieldRepository.countByTemplateId(templateId)).isEqualTo(2);
        assertThat(templateMappingRepository.countByTemplateId(templateId)).isEqualTo(1);

        var fields = templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(templateId);
        var child = fields.stream()
                .filter(field -> "GameKindId".equals(field.getFieldName()))
                .findFirst()
                .orElseThrow();
        var parent = fields.stream()
                .filter(field -> "Game".equals(field.getFieldName()))
                .findFirst()
                .orElseThrow();
        assertThat(child.getParentId()).isEqualTo(parent.getId());

        var mapping = templateMappingRepository.findByTemplateFieldId(child.getId()).orElseThrow();
        assertThat(mapping.getMasterDataFieldId()).isEqualTo(masterDataFieldId);
        assertThat(mapping.getTemplateId()).isEqualTo(templateId);
    }
}
