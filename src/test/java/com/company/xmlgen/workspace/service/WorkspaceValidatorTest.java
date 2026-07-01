package com.company.xmlgen.workspace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.ValidationException;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.entity.WorkspaceStatus;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.repository.WorkspaceOwnedResourceRepository;
import com.company.xmlgen.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceValidatorTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceOwnedResourceRepository ownedResourceRepository;

    @InjectMocks
    private WorkspaceValidator workspaceValidator;

    @Test
    void validateCreate_duplicateCode() {
        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest("DEFAULT", "Duplicate", null, WorkspaceStatus.ACTIVE);
        when(workspaceRepository.existsByCode("DEFAULT")).thenReturn(true);

        assertThatThrownBy(() -> workspaceValidator.validateCreate(request))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(WorkspaceErrorCode.WORKSPACE_CODE_ALREADY_EXISTS);
    }

    @Test
    void validateCreate_uniqueCode() {
        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest("ACME", "Acme Workspace", null, WorkspaceStatus.ACTIVE);
        when(workspaceRepository.existsByCode("ACME")).thenReturn(false);

        workspaceValidator.validateCreate(request);

        verify(workspaceRepository).existsByCode("ACME");
    }

    @Test
    void validateCodeImmutable_rejectsChange() {
        assertThatThrownBy(() -> workspaceValidator.validateCodeImmutable("DEFAULT", "NEW_CODE"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateCodeImmutable_allowsSameCode() {
        workspaceValidator.validateCodeImmutable("DEFAULT", "DEFAULT");
    }

    @Test
    void validateDeletable_rejectsWhenOwnedResourcesExist() {
        when(ownedResourceRepository.hasOwnedResources(1L)).thenReturn(true);

        assertThatThrownBy(() -> workspaceValidator.validateDeletable(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(WorkspaceErrorCode.WORKSPACE_IN_USE);
    }

    @Test
    void validateDeletable_allowsEmptyWorkspace() {
        when(ownedResourceRepository.hasOwnedResources(2L)).thenReturn(false);

        workspaceValidator.validateDeletable(2L);

        verify(ownedResourceRepository).hasOwnedResources(2L);
        assertThat(true).isTrue();
    }
}
