package com.company.xmlgen.workspace.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.exception.ForbiddenException;
import com.company.xmlgen.workspace.context.WorkspaceContext;
import com.company.xmlgen.workspace.context.WorkspaceContextHolder;
import com.company.xmlgen.workspace.domain.WorkspacePermission;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceMemberEntity;
import com.company.xmlgen.workspace.entity.WorkspaceRole;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserPermissionGuardTest {

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private UserPermissionGuard userPermissionGuard;

    @BeforeEach
    void setUp() {
        WorkspaceContextHolder.set(new WorkspaceContext(10L, "DEFAULT"));
    }

    @AfterEach
    void tearDown() {
        WorkspaceContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void systemAdmin_isAlwaysAllowed() {
        authenticate(new AuthenticatedUser(1L, "admin", true));

        assertThatCode(userPermissionGuard::requireTemplateImportPermission).doesNotThrowAnyException();
    }

    @Test
    void membershipWithPermission_isAllowed() {
        authenticate(new AuthenticatedUser(2L, "user", false));
        when(workspaceMemberRepository.findByWorkspace_IdAndUser_Id(10L, 2L))
                .thenReturn(Optional.of(membership(Set.of(WorkspacePermission.IMPORT_TEMPLATE.name()))));

        assertThatCode(userPermissionGuard::requireTemplateImportPermission).doesNotThrowAnyException();
    }

    @Test
    void workspaceAdminWithoutPermission_isDenied() {
        authenticate(new AuthenticatedUser(2L, "user", false));
        when(workspaceMemberRepository.findByWorkspace_IdAndUser_Id(10L, 2L))
                .thenReturn(Optional.of(membership(Set.of())));

        assertThatThrownBy(userPermissionGuard::requireTemplateImportPermission)
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void missingMembership_isDenied() {
        authenticate(new AuthenticatedUser(2L, "user", false));
        when(workspaceMemberRepository.findByWorkspace_IdAndUser_Id(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(userPermissionGuard::requireMasterDataWritePermission)
                .isInstanceOf(ForbiddenException.class);
    }

    private static void authenticate(AuthenticatedUser user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    private static WorkspaceMemberEntity membership(Set<String> permissions) {
        WorkspaceEntity workspace = new WorkspaceEntity(
                "DEFAULT", "Default", WorkspaceStatus.ACTIVE, WorkspaceType.GLOBAL, 1L);
        ReflectionTestUtils.setField(workspace, "id", 10L);
        UserEntity user = new UserEntity("user", "hash", false);
        ReflectionTestUtils.setField(user, "id", 2L);
        WorkspaceMemberEntity member =
                new WorkspaceMemberEntity(workspace, user, WorkspaceRole.WORKSPACE_ADMIN, Instant.now());
        member.setPermissionCodes(permissions);
        return member;
    }
}
