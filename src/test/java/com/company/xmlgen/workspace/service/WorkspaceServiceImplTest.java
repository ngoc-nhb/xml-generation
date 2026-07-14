package com.company.xmlgen.workspace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.authentication.repository.UserRepository;
import com.company.xmlgen.authentication.service.AdminAuthorizationGuard;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.UpdateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceListResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceResponse;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.mapper.WorkspaceMapper;
import com.company.xmlgen.workspace.repository.WorkspaceMemberRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(1L, "admin", true);

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceMapper workspaceMapper;

    @Mock
    private WorkspaceValidator workspaceValidator;

    @Mock
    private AdminAuthorizationGuard adminAuthorizationGuard;

    @Mock
    private PersonalWorkspaceService personalWorkspaceService;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_success() {
        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest("ACME", "Acme Workspace", "Desc", WorkspaceStatus.ACTIVE);
        WorkspaceEntity saved =
                new WorkspaceEntity("ACME", "Acme Workspace", WorkspaceStatus.ACTIVE, WorkspaceType.GLOBAL, 1L);
        saved.setDescription("Desc");
        when(workspaceRepository.save(any(WorkspaceEntity.class))).thenReturn(saved);
        when(workspaceMapper.toCreateResponse(saved))
                .thenReturn(new CreateWorkspaceResponse(
                        2L, "ACME", "Acme Workspace", "Desc", WorkspaceStatus.ACTIVE, WorkspaceType.GLOBAL));

        CreateWorkspaceResponse response = workspaceService.create(request);

        assertThat(response.code()).isEqualTo("ACME");
        verify(workspaceValidator).validateCreate(request);
        verify(adminAuthorizationGuard).requireAdmin();

        ArgumentCaptor<WorkspaceEntity> captor = ArgumentCaptor.forClass(WorkspaceEntity.class);
        verify(workspaceRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("ACME");
        assertThat(captor.getValue().getType()).isEqualTo(WorkspaceType.GLOBAL);
        assertThat(captor.getValue().getCreatedById()).isEqualTo(1L);
    }

    @Test
    void create_duplicateCode() {
        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest("DEFAULT", "Duplicate", null, WorkspaceStatus.ACTIVE);
        org.mockito.Mockito.doThrow(new ConflictException(WorkspaceErrorCode.WORKSPACE_CODE_ALREADY_EXISTS))
                .when(workspaceValidator)
                .validateCreate(request);

        assertThatThrownBy(() -> workspaceService.create(request)).isInstanceOf(ConflictException.class);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void findById_success() {
        WorkspaceEntity entity = activeWorkspace(1L, "DEFAULT", "Default Workspace");
        when(workspaceRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entity));
        when(workspaceMapper.toResponse(entity))
                .thenReturn(new WorkspaceResponse(
                        1L,
                        "DEFAULT",
                        "Default Workspace",
                        null,
                        WorkspaceStatus.ACTIVE,
                        WorkspaceType.GLOBAL,
                        1L,
                        null,
                        null));

        WorkspaceResponse response = workspaceService.findById(1L);

        assertThat(response.code()).isEqualTo("DEFAULT");
    }

    @Test
    void findById_notFound() {
        when(workspaceRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(WorkspaceErrorCode.WORKSPACE_NOT_FOUND);
    }

    @Test
    void update_success() {
        WorkspaceEntity entity = activeWorkspace(2L, "ACME", "Acme Workspace");
        when(workspaceRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(entity));
        when(workspaceRepository.save(entity)).thenReturn(entity);
        when(workspaceMapper.toUpdateResponse(entity))
                .thenReturn(new UpdateWorkspaceResponse(
                        2L, "ACME", "Acme Updated", "Updated", WorkspaceStatus.INACTIVE, WorkspaceType.GLOBAL));

        UpdateWorkspaceRequest request = new UpdateWorkspaceRequest("Acme Updated", "Updated", WorkspaceStatus.INACTIVE);
        UpdateWorkspaceResponse response = workspaceService.update(2L, request);

        assertThat(response.name()).isEqualTo("Acme Updated");
        assertThat(entity.getCode()).isEqualTo("ACME");
        assertThat(entity.getStatus()).isEqualTo(WorkspaceStatus.INACTIVE);
    }

    @Test
    void delete_success() {
        WorkspaceEntity entity = activeWorkspace(2L, "ACME", "Acme Workspace");
        when(workspaceRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(entity));
        when(workspaceRepository.save(entity)).thenReturn(entity);

        workspaceService.delete(2L);

        verify(workspaceValidator).validateDeletable(2L);
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_inUse() {
        WorkspaceEntity entity = activeWorkspace(1L, "DEFAULT", "Default Workspace");
        when(workspaceRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entity));
        org.mockito.Mockito.doThrow(new BusinessException(WorkspaceErrorCode.WORKSPACE_IN_USE))
                .when(workspaceValidator)
                .validateDeletable(1L);

        assertThatThrownBy(() -> workspaceService.delete(1L)).isInstanceOf(BusinessException.class);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void findAll_success() {
        WorkspaceEntity entity = activeWorkspace(1L, "DEFAULT", "Default Workspace");
        Page<WorkspaceEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);
        when(workspaceRepository.findByDeletedAtIsNull(any(Pageable.class))).thenReturn(page);
        when(workspaceMapper.toListResponse(entity, null, null))
                .thenReturn(new WorkspaceListResponse(
                        1L, "DEFAULT", "Default Workspace", WorkspaceStatus.ACTIVE, WorkspaceType.GLOBAL, null, null));

        PageResult<WorkspaceListResponse> result = workspaceService.findAll(1, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.meta().totalRecords()).isEqualTo(1);
    }

    private static WorkspaceEntity activeWorkspace(Long id, String code, String name) {
        WorkspaceEntity entity =
                new WorkspaceEntity(code, name, WorkspaceStatus.ACTIVE, WorkspaceType.GLOBAL, 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "id", id);
        entity.setDeletedAt(null);
        return entity;
    }
}
