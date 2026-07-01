package com.company.xmlgen.workspace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.workspace.context.WorkspaceContext;
import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceContextResolverTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private WorkspaceContextResolver workspaceContextResolver;

    @Test
    void resolve_activeWorkspace() {
        WorkspaceEntity entity = new WorkspaceEntity("DEFAULT", "Default Workspace", WorkspaceStatus.ACTIVE, 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(entity, "id", 1L);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entity));

        WorkspaceContext context = workspaceContextResolver.resolve(1L);

        assertThat(context.workspaceId()).isEqualTo(1L);
        assertThat(context.workspaceCode()).isEqualTo("DEFAULT");
    }

    @Test
    void resolve_unknownWorkspace() {
        when(workspaceRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceContextResolver.resolve(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(WorkspaceErrorCode.INVALID_WORKSPACE);
    }

    @Test
    void resolve_inactiveWorkspace() {
        WorkspaceEntity entity = new WorkspaceEntity("INACTIVE", "Inactive", WorkspaceStatus.INACTIVE, 1L);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> workspaceContextResolver.resolve(2L))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(WorkspaceErrorCode.WORKSPACE_INACTIVE);
    }
}
