package com.company.xmlgen.savedinput.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.savedinput.dto.response.SavedInputResponse;
import com.company.xmlgen.savedinput.entity.SavedInputEntity;
import com.company.xmlgen.savedinput.exception.SavedInputErrorCode;
import com.company.xmlgen.savedinput.repository.SavedInputRepository;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for Saved Input lifecycle.
 */
@Service
public class SavedInputServiceImpl implements SavedInputService {

    private final SavedInputRepository savedInputRepository;
    private final WorkspaceOwnershipGuard workspaceOwnershipGuard;

    public SavedInputServiceImpl(
            SavedInputRepository savedInputRepository, WorkspaceOwnershipGuard workspaceOwnershipGuard) {
        this.savedInputRepository = savedInputRepository;
        this.workspaceOwnershipGuard = workspaceOwnershipGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public SavedInputResponse findByTemplate(Long templateId) {
        workspaceOwnershipGuard.requireTemplate(templateId);
        long workspaceId = workspaceOwnershipGuard.currentWorkspaceId();
        long userId = getCurrentUser().id();

        SavedInputEntity entity = savedInputRepository
                .findByWorkspaceIdAndUserIdAndTemplateId(workspaceId, userId, templateId)
                .orElseThrow(() -> new NotFoundException(SavedInputErrorCode.SAVED_INPUT_NOT_FOUND));

        return toResponse(entity);
    }

    @Override
    @Transactional
    public void saveFromExport(Long templateId, JsonNode inputData, JsonNode selectedMasterData) {
        workspaceOwnershipGuard.requireTemplate(templateId);
        long workspaceId = workspaceOwnershipGuard.currentWorkspaceId();
        long userId = getCurrentUser().id();

        JsonNode safeInputData = inputData == null || inputData.isNull() ? NullNode.instance : inputData;
        JsonNode safeSelectedMasterData =
                selectedMasterData == null || selectedMasterData.isNull() ? null : selectedMasterData;

        SavedInputEntity entity = savedInputRepository
                .findByWorkspaceIdAndUserIdAndTemplateId(workspaceId, userId, templateId)
                .orElseGet(() -> new SavedInputEntity(workspaceId, userId, templateId, safeInputData, safeSelectedMasterData));

        entity.setInputDataJson(safeInputData);
        entity.setSelectedMasterDataJson(safeSelectedMasterData);
        savedInputRepository.save(entity);
    }

    private static SavedInputResponse toResponse(SavedInputEntity entity) {
        return new SavedInputResponse(
                entity.getTemplateId(),
                entity.getInputDataJson(),
                entity.getSelectedMasterDataJson(),
                entity.getUpdatedAt());
    }

    private static AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
