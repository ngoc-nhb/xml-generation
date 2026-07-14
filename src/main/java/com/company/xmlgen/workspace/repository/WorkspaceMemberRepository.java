package com.company.xmlgen.workspace.repository;

import com.company.xmlgen.workspace.entity.WorkspaceMemberEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link WorkspaceMemberEntity}.
 *
 * @see docs/03-database-design/03-database-design.md §4.12
 */
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMemberEntity, Long> {

    List<WorkspaceMemberEntity> findByWorkspace_Id(Long workspaceId);

    List<WorkspaceMemberEntity> findByUser_Id(Long userId);

    Optional<WorkspaceMemberEntity> findByWorkspace_IdAndUser_Id(Long workspaceId, Long userId);

    boolean existsByWorkspace_IdAndUser_Id(Long workspaceId, Long userId);
}
