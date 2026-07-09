package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.service.TemplateService;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.service.WorkspaceService;
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
class WorkspaceIsolationIntegrationTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private WorkspaceService workspaceService;

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
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void templateInAnotherWorkspaceIsNotAccessible() {
        String code = "ISO_" + UUID.randomUUID().toString().substring(0, 8);
        Long templateId = templateService
                .create(new CreateTemplateRequest(code, "Workspace A Template", null, null, null))
                .id();

        var otherWorkspace = workspaceService.create(new CreateWorkspaceRequest(
                "WS_" + UUID.randomUUID().toString().substring(0, 8),
                "Workspace B",
                null,
                WorkspaceStatus.ACTIVE));

        WorkspaceTestSupport.useWorkspace(otherWorkspace.id(), otherWorkspace.code());

        assertThatThrownBy(() -> templateService.findById(templateId))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);
    }
}
