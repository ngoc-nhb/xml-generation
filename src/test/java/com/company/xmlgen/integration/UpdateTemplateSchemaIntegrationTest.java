package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.ValidationException;
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
import com.company.xmlgen.template.dto.request.UpdateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.response.TemplateSchemaResponse;
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
class UpdateTemplateSchemaIntegrationTest {

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
    void updateSchema_emptyPayload_clearsMetadata() {
        Long templateId = createTemplateWithMetadata();

        TemplateSchemaResponse response =
                templateService.updateSchema(templateId, new UpdateTemplateSchemaRequest(null, List.of(), List.of()));

        assertThat(response).isNull();
        assertThat(templateFieldRepository.countByTemplateId(templateId)).isZero();
        assertThat(templateMappingRepository.countByTemplateId(templateId)).isZero();
        assertThat(templateRepository.findById(templateId).orElseThrow().getCompiledSchemaJson()).isNull();
        assertThat(templateService.findById(templateId).schema()).isNull();
    }

    @Test
    void updateSchema_replacesHierarchyAndMappings() {
        Long templateId = createTemplateWithMetadata();
        Long masterDataFieldId = createMasterDataField();

        CreateTemplateFieldRequest root = field("Report", null, "Report", TemplateFieldNodeType.GROUP, 1);
        CreateTemplateFieldRequest child = field(
                "ReportId",
                "Report",
                "ReportID",
                TemplateFieldNodeType.ELEMENT,
                TemplateFieldSourceType.INPUT,
                1);
        UpdateTemplateSchemaRequest request = new UpdateTemplateSchemaRequest(
                null,
                List.of(root, child),
                List.of(new CreateTemplateMappingRequest("ReportId", masterDataFieldId)));

        TemplateSchemaResponse response = templateService.updateSchema(templateId, request);

        assertThat(response.fields()).hasSize(2);
        assertThat(response.fields().get(1).parentFieldName()).isEqualTo("Report");
        assertThat(response.mappings()).hasSize(1);
        assertThat(response.mappings().get(0).fieldName()).isEqualTo("ReportId");
        assertThat(templateFieldRepository.countByTemplateId(templateId)).isEqualTo(2);
        assertThat(templateMappingRepository.countByTemplateId(templateId)).isEqualTo(1);
        var compiledSchema = templateRepository.findById(templateId).orElseThrow().getCompiledSchemaJson();
        assertThat(compiledSchema).isNotNull();
        assertThat(compiledSchema.at("/roots/0/fieldName").asText()).isEqualTo("Report");
        assertThat(compiledSchema.at("/roots/0/children/0/fieldName").asText()).isEqualTo("ReportId");
        assertThat(compiledSchema.at("/mappings/0/fieldName").asText()).isEqualTo("ReportId");
        assertThat(templateService.findById(templateId).schema().fields()).hasSize(2);
    }

    @Test
    void updateSchema_allowsDuplicateDisplayOrderUnderDifferentParents() {
        Long templateId = createTemplateWithMetadata();

        CreateTemplateFieldRequest football = field("Football", null, "Football", TemplateFieldNodeType.ELEMENT, 1);
        CreateTemplateFieldRequest commentReport =
                field("CommentReport", "Football", "CommentReport", TemplateFieldNodeType.ELEMENT, 1);
        CreateTemplateFieldRequest gameId =
                field("GameID", "CommentReport", "GameID", TemplateFieldNodeType.ELEMENT, 1);
        UpdateTemplateSchemaRequest request =
                new UpdateTemplateSchemaRequest(null, List.of(football, commentReport, gameId), List.of());

        TemplateSchemaResponse response = templateService.updateSchema(templateId, request);

        assertThat(response.fields()).hasSize(3);
        assertThat(templateFieldRepository.countByTemplateId(templateId)).isEqualTo(3);
    }

    @Test
    void updateSchema_validationFailure_preservesExistingMetadata() {
        Long templateId = createTemplateWithMetadata();
        long fieldCountBefore = templateFieldRepository.countByTemplateId(templateId);
        long mappingCountBefore = templateMappingRepository.countByTemplateId(templateId);

        CreateTemplateFieldRequest duplicate = field("Game", null, "Game", TemplateFieldNodeType.GROUP, 1);
        UpdateTemplateSchemaRequest request =
                new UpdateTemplateSchemaRequest(null, List.of(duplicate, duplicate), List.of());

        assertThatThrownBy(() -> templateService.updateSchema(templateId, request))
                .isInstanceOf(ValidationException.class);

        assertThat(templateFieldRepository.countByTemplateId(templateId)).isEqualTo(fieldCountBefore);
        assertThat(templateMappingRepository.countByTemplateId(templateId)).isEqualTo(mappingCountBefore);
    }

    private Long createTemplateWithMetadata() {
        String code = "UPDATE_SCHEMA_" + UUID.randomUUID();
        CreateTemplateSchemaRequest schema = new CreateTemplateSchemaRequest(
                List.of(
                        field("Game", null, "Game", TemplateFieldNodeType.GROUP, 1),
                        field(
                                "GameId",
                                "Game",
                                "GameID",
                                TemplateFieldNodeType.ELEMENT,
                                TemplateFieldSourceType.INPUT,
                                1)),
                List.of());
        return templateService
                .create(new CreateTemplateRequest(code, "Template", "description", schema))
                .id();
    }

    private Long createMasterDataField() {
        String typeCode = "MDT_" + UUID.randomUUID();
        return masterDataFieldService
                .create(new CreateMasterDataFieldRequest(
                        masterDataTypeService
                                .create(new CreateMasterDataTypeRequest(
                                        typeCode, "Type", null, MasterDataTypeStatus.ACTIVE))
                                .id(),
                        "field_id",
                        "Field",
                        MasterDataFieldDataType.INTEGER,
                        true,
                        1,
                        null,
                        null,
                        true,
                        true,
                        null))
                .id();
    }

    private static CreateTemplateFieldRequest field(
            String fieldName, String parentFieldName, String xmlName, TemplateFieldNodeType nodeType, int displayOrder) {
        return field(fieldName, parentFieldName, xmlName, nodeType, null, displayOrder);
    }

    private static CreateTemplateFieldRequest field(
            String fieldName,
            String parentFieldName,
            String xmlName,
            TemplateFieldNodeType nodeType,
            TemplateFieldSourceType sourceType,
            int displayOrder) {
        return new CreateTemplateFieldRequest(
                fieldName,
                parentFieldName,
                xmlName,
                fieldName,
                nodeType,
                null,
                sourceType,
                null,
                TemplateFieldEmptyHandling.REQUIRED,
                null,
                null,
                null,
                null,
                null,
                null,
                displayOrder,
                null);
    }
}
