package com.company.xmlgen.savedinput.repository;

import com.company.xmlgen.savedinput.entity.SavedInputEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedInputRepository extends JpaRepository<SavedInputEntity, Long> {

    Optional<SavedInputEntity> findByWorkspaceIdAndUserIdAndTemplateId(
            Long workspaceId, Long userId, Long templateId);
}
