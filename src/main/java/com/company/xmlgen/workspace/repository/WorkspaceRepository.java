package com.company.xmlgen.workspace.repository;

import com.company.xmlgen.workspace.entity.WorkspaceEntity;
import com.company.xmlgen.workspace.entity.WorkspaceType;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link WorkspaceEntity}.
 *
 * @see docs/03-database-design/03-database-design.md §4.11
 */
public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, Long> {

    Optional<WorkspaceEntity> findByCode(String code);

    boolean existsByCode(String code);

    Optional<WorkspaceEntity> findByIdAndDeletedAtIsNull(Long id);

    Page<WorkspaceEntity> findByDeletedAtIsNull(Pageable pageable);

    Page<WorkspaceEntity> findByDeletedAtIsNullAndIdIn(Collection<Long> ids, Pageable pageable);

    boolean existsByCreatedByIdAndTypeAndDeletedAtIsNull(Long createdById, WorkspaceType type);

    Optional<WorkspaceEntity> findByCreatedByIdAndTypeAndDeletedAtIsNull(
            Long createdById, WorkspaceType type);
}
