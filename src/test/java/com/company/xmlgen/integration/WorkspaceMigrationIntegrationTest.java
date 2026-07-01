package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfig.class)
@EnabledIf("isDockerAvailable")
class WorkspaceMigrationIntegrationTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void defaultWorkspaceExistsAfterMigration() {
        var workspace = workspaceRepository.findByCode("DEFAULT").orElseThrow();
        assertThat(workspace.getId()).isEqualTo(1L);
        assertThat(workspace.getName()).isEqualTo("Default Workspace");
        assertThat(workspace.getStatus()).isEqualTo(WorkspaceStatus.ACTIVE);
        assertThat(workspaceMemberRepository.findByWorkspace_Id(workspace.getId())).isNotEmpty();
    }
}
