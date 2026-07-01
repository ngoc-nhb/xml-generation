package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.masterdata.dto.request.CreateMasterDataTypeRequest;
import com.company.xmlgen.masterdata.entity.MasterDataTypeStatus;
import com.company.xmlgen.masterdata.service.MasterDataTypeService;
import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.UpdateWorkspaceResponse;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
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
class WorkspaceCrudIntegrationTest {

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private MasterDataTypeService masterDataTypeService;

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
    void create_find_update_deleteLifecycle() {
        String code = "WS_" + UUID.randomUUID().toString().substring(0, 8);

        CreateWorkspaceResponse created = workspaceService.create(
                new CreateWorkspaceRequest(code, "Test Workspace", "Integration test", WorkspaceStatus.ACTIVE));

        assertThat(created.id()).isNotNull();
        assertThat(created.code()).isEqualTo(code);

        var found = workspaceService.findById(created.id());
        assertThat(found.code()).isEqualTo(code);
        assertThat(found.name()).isEqualTo("Test Workspace");
        assertThat(found.createdById()).isEqualTo(1L);

        UpdateWorkspaceResponse updated = workspaceService.update(
                created.id(),
                new UpdateWorkspaceRequest("Test Workspace Updated", "Updated description", WorkspaceStatus.INACTIVE));
        assertThat(updated.code()).isEqualTo(code);
        assertThat(updated.name()).isEqualTo("Test Workspace Updated");
        assertThat(updated.status()).isEqualTo(WorkspaceStatus.INACTIVE);

        workspaceService.delete(created.id());

        var deleted = workspaceRepository.findById(created.id()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
        assertThatThrownBy(() -> workspaceService.findById(created.id()))
                .isInstanceOf(com.company.xmlgen.exception.NotFoundException.class);
    }

    @Test
    void create_duplicateCodeRejected() {
        assertThatThrownBy(() -> workspaceService.create(
                        new CreateWorkspaceRequest("DEFAULT", "Duplicate", null, WorkspaceStatus.ACTIVE)))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(WorkspaceErrorCode.WORKSPACE_CODE_ALREADY_EXISTS);
    }

    @Test
    void delete_defaultWorkspaceRejectedWhenInUse() {
        masterDataTypeService.create(new CreateMasterDataTypeRequest(
                "WS_GUARD_" + UUID.randomUUID().toString().substring(0, 8),
                "Delete Guard Type",
                null,
                MasterDataTypeStatus.ACTIVE));

        assertThatThrownBy(() -> workspaceService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(WorkspaceErrorCode.WORKSPACE_IN_USE);
    }

    @Test
    void findAll_includesDefaultWorkspace() {
        var result = workspaceService.findAll(1, 20);
        assertThat(result.content()).anyMatch(item -> "DEFAULT".equals(item.code()));
    }
}
