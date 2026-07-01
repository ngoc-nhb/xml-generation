package com.company.xmlgen.masterdata.repository;

import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link MasterDataTypeEntity}.
 *
 * @see docs/11-implementation-guide/master-data.md
 */
public interface MasterDataTypeRepository extends JpaRepository<MasterDataTypeEntity, Long> {

    Optional<MasterDataTypeEntity> findByIdAndWorkspaceId(Long id, Long workspaceId);

    boolean existsByWorkspaceIdAndCode(Long workspaceId, String code);

    Page<MasterDataTypeEntity> findByWorkspaceId(Long workspaceId, Pageable pageable);

    Page<MasterDataTypeEntity> findByWorkspaceIdAndNameContainingIgnoreCase(
            Long workspaceId, String name, Pageable pageable);
}
