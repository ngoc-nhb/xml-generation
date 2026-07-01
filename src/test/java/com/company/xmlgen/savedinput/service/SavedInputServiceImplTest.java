package com.company.xmlgen.savedinput.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.savedinput.entity.SavedInputEntity;
import com.company.xmlgen.savedinput.exception.SavedInputErrorCode;
import com.company.xmlgen.savedinput.repository.SavedInputRepository;
import com.company.xmlgen.support.WorkspaceTestSupport;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SavedInputServiceImplTest {

    private static final Long WORKSPACE_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final Long TEMPLATE_ID = 10L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private SavedInputRepository savedInputRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private com.company.xmlgen.masterdata.repository.MasterDataTypeRepository masterDataTypeRepository;

    @Mock
    private com.company.xmlgen.masterdata.repository.MasterDataFieldRepository masterDataFieldRepository;

    @Mock
    private com.company.xmlgen.masterdata.repository.MasterDataRecordRepository masterDataRecordRepository;

    private SavedInputService savedInputService;

    @BeforeEach
    void setUp() {
        WorkspaceOwnershipGuard workspaceOwnershipGuard = new WorkspaceOwnershipGuard(
                templateRepository,
                masterDataTypeRepository,
                masterDataFieldRepository,
                masterDataRecordRepository);
        savedInputService = new SavedInputServiceImpl(savedInputRepository, workspaceOwnershipGuard);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(USER_ID, "user", false), null));
        WorkspaceTestSupport.useDefaultWorkspace();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        WorkspaceTestSupport.clearWorkspace();
    }

    @Test
    void findByTemplate_returnsSavedInput() throws Exception {
        stubTemplate();
        SavedInputEntity entity = savedEntity();
        when(savedInputRepository.findByWorkspaceIdAndUserIdAndTemplateId(WORKSPACE_ID, USER_ID, TEMPLATE_ID))
                .thenReturn(Optional.of(entity));

        var response = savedInputService.findByTemplate(TEMPLATE_ID);

        assertThat(response.templateId()).isEqualTo(TEMPLATE_ID);
        assertThat(response.inputData().get("GameId").asInt()).isEqualTo(123);
        assertThat(response.selectedMasterData().get("GAME_KIND").get("id").asInt()).isEqualTo(2);
        assertThat(response.updatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    void findByTemplate_missingSavedInput_throwsNotFound() {
        stubTemplate();
        when(savedInputRepository.findByWorkspaceIdAndUserIdAndTemplateId(WORKSPACE_ID, USER_ID, TEMPLATE_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> savedInputService.findByTemplate(TEMPLATE_ID))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(SavedInputErrorCode.SAVED_INPUT_NOT_FOUND);
    }

    @Test
    void saveFromExport_createsNewSavedInput() throws Exception {
        stubTemplate();
        when(savedInputRepository.findByWorkspaceIdAndUserIdAndTemplateId(WORKSPACE_ID, USER_ID, TEMPLATE_ID))
                .thenReturn(Optional.empty());
        when(savedInputRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ObjectNode inputData = OBJECT_MAPPER.createObjectNode().put("GameId", 99);
        ObjectNode selectedMasterData = OBJECT_MAPPER.createObjectNode();
        selectedMasterData.putObject("GAME_KIND").put("id", 3);

        savedInputService.saveFromExport(TEMPLATE_ID, inputData, selectedMasterData);

        ArgumentCaptor<SavedInputEntity> captor = ArgumentCaptor.forClass(SavedInputEntity.class);
        verify(savedInputRepository).save(captor.capture());
        SavedInputEntity saved = captor.getValue();
        assertThat(saved.getWorkspaceId()).isEqualTo(WORKSPACE_ID);
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getTemplateId()).isEqualTo(TEMPLATE_ID);
        assertThat(saved.getInputDataJson().get("GameId").asInt()).isEqualTo(99);
        assertThat(saved.getSelectedMasterDataJson().get("GAME_KIND").get("id").asInt()).isEqualTo(3);
    }

    @Test
    void saveFromExport_overwritesExistingSavedInput() throws Exception {
        stubTemplate();
        SavedInputEntity existing = savedEntity();
        when(savedInputRepository.findByWorkspaceIdAndUserIdAndTemplateId(WORKSPACE_ID, USER_ID, TEMPLATE_ID))
                .thenReturn(Optional.of(existing));
        when(savedInputRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ObjectNode inputData = OBJECT_MAPPER.createObjectNode().put("GameId", 777);
        savedInputService.saveFromExport(TEMPLATE_ID, inputData, null);

        assertThat(existing.getInputDataJson().get("GameId").asInt()).isEqualTo(777);
        assertThat(existing.getSelectedMasterDataJson()).isNull();
        verify(savedInputRepository).save(existing);
    }

    private void stubTemplate() {
        TemplateEntity template = new TemplateEntity("CODE", "Name", TemplateStatus.ACTIVE, USER_ID);
        template.setWorkspaceId(WORKSPACE_ID);
        when(templateRepository.findByIdAndWorkspaceId(TEMPLATE_ID, WORKSPACE_ID)).thenReturn(Optional.of(template));
    }

    private SavedInputEntity savedEntity() throws Exception {
        ObjectNode inputData = OBJECT_MAPPER.createObjectNode().put("GameId", 123);
        ObjectNode selectedMasterData = OBJECT_MAPPER.createObjectNode();
        selectedMasterData.putObject("GAME_KIND").put("id", 2);
        SavedInputEntity entity =
                new SavedInputEntity(WORKSPACE_ID, USER_ID, TEMPLATE_ID, inputData, selectedMasterData);
        return entity;
    }
}
