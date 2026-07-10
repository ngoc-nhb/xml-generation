package com.company.xmlgen.investigation;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.template.dto.request.CreateTemplateFieldRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import com.company.xmlgen.template.importing.domain.XmlImportNode;
import com.company.xmlgen.template.importing.dto.response.TemplateImportDraftFieldResponse;
import com.company.xmlgen.template.importing.service.TemplateDraftBuilderImpl;
import com.company.xmlgen.template.importing.service.TemplateImportSampleInputBuilder;
import com.company.xmlgen.template.importing.service.XmlImportParserImpl;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.template.service.TemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
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

/**
 * Investigation-only test. Prints actual data at each backend checkpoint.
 * Not an acceptance test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfig.class)
@EnabledIf("isDockerAvailable")
class ImportedSampleDataE2EInvestigationTest {

    private static final String FOOTBALL_XML =
            """
            <Football>
              <GameSchedule>
                <SendDate>20260101</SendDate>
                <Year>2026</Year>
                <GameKindID>2</GameKindID>
                <GameKindName>J1</GameKindName>
                <GameCategory>
                  <SeasonID>10</SeasonID>
                  <SeasonName>Spring</SeasonName>
                  <CupName>Cup A</CupName>
                  <GameC>G1</GameC>
                </GameCategory>
                <GameCategory>
                  <SeasonID>11</SeasonID>
                  <SeasonName>Summer</SeasonName>
                  <CupName>Cup B</CupName>
                  <GameC>G2</GameC>
                </GameCategory>
                <GameCategory>
                  <SeasonID>12</SeasonID>
                  <SeasonName>Fall</SeasonName>
                  <CupName>Cup C</CupName>
                  <GameC>G3</GameC>
                </GameCategory>
              </GameSchedule>
              <ScheduleInfo>
                <Schedule ScheduleNo="1"><GameID>G1</GameID></Schedule>
                <Schedule ScheduleNo="2"><GameID>G2</GameID></Schedule>
                <Schedule ScheduleNo="3"><GameID>G3</GameID></Schedule>
                <Schedule ScheduleNo="4"><GameID>G4</GameID></Schedule>
                <Schedule ScheduleNo="5"><GameID>G5</GameID></Schedule>
              </ScheduleInfo>
            </Football>
            """;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateRepository templateRepository;

    private final ObjectMapper objectMapper =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

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
    void investigate_backendCheckpoints_printActualData() throws Exception {
        XmlImportParserImpl parser = new XmlImportParserImpl();
        TemplateDraftBuilderImpl draftBuilder = new TemplateDraftBuilderImpl();
        TemplateImportSampleInputBuilder sampleBuilder = new TemplateImportSampleInputBuilder(objectMapper);

        XmlImportNode root = parser.parse(FOOTBALL_XML.getBytes(StandardCharsets.UTF_8));
        int scheduleXmlNodes = countXmlElementChildren(findXmlChild(root, "ScheduleInfo"), "Schedule");

        XmlImportNode gameSchedule = findXmlChild(root, "GameSchedule");
        int gameCategoryXmlNodes = gameSchedule == null ? 0 : countXmlElementChildren(gameSchedule, "GameCategory");
        System.out.println("CHECKPOINT: XML Parser");
        System.out.println("  Status: PASS");
        System.out.println("  Schedule XML node count: " + scheduleXmlNodes);
        System.out.println("  GameCategory XML node count under GameSchedule: " + gameCategoryXmlNodes);

        List<TemplateImportDraftFieldResponse> draftFields = draftBuilder.build(root);
        var scheduleField = draftFields.stream()
                .filter(f -> "Schedule".equals(f.fieldName()))
                .findFirst()
                .orElseThrow();
        var gameCategoryField = draftFields.stream()
                .filter(f -> "GameCategory".equals(f.fieldName()))
                .findFirst()
                .orElseThrow();

        JsonNode sampleJson = sampleBuilder.build(root, draftFields);
        System.out.println("CHECKPOINT: TemplateImportSampleInputBuilder");
        System.out.println("  Status: PASS");
        System.out.println("  Schedule field occurrence in schema draft: " + scheduleField.occurrenceRule());
        System.out.println("  GameCategory field occurrence in schema draft: " + gameCategoryField.occurrenceRule());
        System.out.println("  sampleInputJson ScheduleInfo.Schedule array size: "
                + sampleJson.get("ScheduleInfo").get("Schedule").size());
        System.out.println("  sampleInputJson GameCategory array size: "
                + sampleJson.get("GameSchedule").get("GameCategory").size());
        System.out.println("  sampleInputJson GameCategory[0].SeasonID: "
                + sampleJson.get("GameSchedule").get("GameCategory").get(0).get("SeasonID").asText());
        System.out.println("  sampleInputJson FULL:");
        System.out.println(objectMapper.writeValueAsString(sampleJson));

        CreateTemplateSchemaRequest schema = new CreateTemplateSchemaRequest(
                draftFields.stream().map(this::toCreateField).toList(),
                List.of());

        String code = "INV_E2E_" + UUID.randomUUID();
        Long templateId = templateService
                .create(new CreateTemplateRequest(code, "Investigation", null, schema, sampleJson))
                .id();

        JsonNode dbSample = templateRepository.findById(templateId).orElseThrow().getSampleInputJson();
        System.out.println("CHECKPOINT: Database (templates.sample_input_json)");
        System.out.println("  Status: PASS");
        System.out.println("  Schedule array size: " + dbSample.get("ScheduleInfo").get("Schedule").size());
        System.out.println("  GameCategory array size: " + dbSample.get("GameSchedule").get("GameCategory").size());
        System.out.println("  SeasonID present: " + dbSample.get("GameSchedule").get("GameCategory").get(0).has("SeasonID"));
        System.out.println("  sample_input_json FULL:");
        System.out.println(objectMapper.writeValueAsString(dbSample));

        TemplateResponse apiResponse = templateService.findById(templateId);
        System.out.println("CHECKPOINT: GET Template API (TemplateService.findById)");
        System.out.println("  Status: PASS");
        System.out.println("  sampleInputJson ScheduleInfo.Schedule array size: "
                + apiResponse.sampleInputJson().get("ScheduleInfo").get("Schedule").size());
        System.out.println("  sampleInputJson GameCategory array size: "
                + apiResponse.sampleInputJson().get("GameSchedule").get("GameCategory").size());
        System.out.println("  API sampleInputJson FULL:");
        System.out.println(objectMapper.writeValueAsString(apiResponse.sampleInputJson()));
    }

    private CreateTemplateFieldRequest toCreateField(TemplateImportDraftFieldResponse field) {
        return new CreateTemplateFieldRequest(
                field.fieldName(),
                field.parentFieldName(),
                field.xmlName(),
                field.displayName(),
                field.nodeType(),
                field.valueType(),
                field.sourceType(),
                field.occurrenceRule(),
                field.emptyHandling(),
                field.requiredWhenParentExists(),
                field.triggerActivation(),
                field.defaultValue(),
                field.staticValue(),
                field.xmlPath(),
                field.namespace(),
                field.displayOrder(),
                field.description());
    }

    private static XmlImportNode findXmlChild(XmlImportNode parent, String name) {
        for (XmlImportNode child : parent.getChildren()) {
            if (name.equals(child.getNodeName())) {
                return child;
            }
        }
        return null;
    }

    private static int countXmlElementChildren(XmlImportNode parent, String name) {
        int count = 0;
        for (XmlImportNode child : parent.getChildren()) {
            if (name.equals(child.getNodeName())) {
                count++;
            }
        }
        return count;
    }
}
