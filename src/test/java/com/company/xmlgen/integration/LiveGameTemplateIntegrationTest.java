package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateSchemaRequest;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.template.service.TemplateService;
import com.company.xmlgen.xmlgeneration.dto.PreviewRequest;
import com.company.xmlgen.xmlgeneration.dto.PreviewResponse;
import com.company.xmlgen.xmlgeneration.service.PreviewService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
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
class LiveGameTemplateIntegrationTest {

    private static final Path TEST_DATA = Path.of("test_data");

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private PreviewService previewService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void liveGameTemplate_compilesAndPreviewsFromTestData() throws Exception {
        JsonNode createPayload = objectMapper.readTree(TEST_DATA.resolve("live_game_create_template.json").toFile());
        JsonNode previewPayload = objectMapper.readTree(TEST_DATA.resolve("live_game_input.json").toFile());

        String code = "LIVE_GAME_TEST_" + UUID.randomUUID();
        CreateTemplateSchemaRequest schema =
                objectMapper.treeToValue(createPayload.get("schema"), CreateTemplateSchemaRequest.class);
        Long templateId = templateService
                .create(new CreateTemplateRequest(
                        code,
                        createPayload.get("name").asText(),
                        createPayload.get("description").asText(),
                        schema))
                .id();

        var template = templateRepository.findById(templateId).orElseThrow();
        assertThat(template.getCompiledSchemaJson()).isNotNull();
        assertThat(template.getCompiledSchemaJson().get("roots")).isNotEmpty();

        JsonNode inputData = previewPayload.get("inputData");
        JsonNode selectedMasterData = previewPayload.get("selectedMasterData");
        PreviewResponse response = previewService.preview(new PreviewRequest(templateId, inputData, selectedMasterData));

        assertThat(response.successful())
                .withFailMessage("Preview failed: %s", response.validationErrors())
                .isTrue();
        assertThat(response.validationErrors()).isEmpty();
        assertThat(response.xml()).contains("<Football>");
        assertThat(response.xml()).contains("<GameReport>");
        assertThat(response.xml()).contains("<GameID>2026062339</GameID>");
        assertThat(response.xml()).contains("<TeamInfo");
        assertThat(response.xml()).contains("<GoalInfo");
    }
}
