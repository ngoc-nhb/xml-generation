package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.workspace.context.WorkspaceContextHeaders;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfig.class)
@EnabledIf("isDockerAvailable")
class WorkspaceContextIntegrationTest {

    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(1L, "admin", true);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN, null, null));
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
        } catch (Exception e) {
            return false;
        }
    }

    private org.springframework.security.core.Authentication adminAuthentication() {
        return new UsernamePasswordAuthenticationToken(ADMIN, null, java.util.List.of());
    }

    @Test
    void missingWorkspaceHeaderRejected() throws Exception {
        mockMvc.perform(get("/api/v1/templates").with(authentication(adminAuthentication())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].code").value("WORKSPACE_REQUIRED"));
    }

    @Test
    void validWorkspaceHeaderAllowsRequest() throws Exception {
        mockMvc.perform(get("/api/v1/templates")
                        .with(authentication(adminAuthentication()))
                        .header(WorkspaceContextHeaders.WORKSPACE_ID, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void unknownWorkspaceRejected() throws Exception {
        mockMvc.perform(get("/api/v1/templates")
                        .with(authentication(adminAuthentication()))
                        .header(WorkspaceContextHeaders.WORKSPACE_ID, "99999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("INVALID_WORKSPACE"));
    }

    @Test
    void inactiveWorkspaceRejected() throws Exception {
        WorkspaceEntity inactive = new WorkspaceEntity(
                "INACTIVE_CTX", "Inactive Context Test", WorkspaceStatus.INACTIVE, WorkspaceType.GLOBAL, 1L);
        WorkspaceEntity saved = workspaceRepository.save(inactive);

        mockMvc.perform(get("/api/v1/templates")
                        .with(authentication(adminAuthentication()))
                        .header(WorkspaceContextHeaders.WORKSPACE_ID, String.valueOf(saved.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].code").value("WORKSPACE_INACTIVE"));
    }

    @Test
    void workspaceIdQueryParameterFallbackWorks() throws Exception {
        mockMvc.perform(get("/api/v1/templates")
                        .with(authentication(adminAuthentication()))
                        .queryParam("workspaceId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
