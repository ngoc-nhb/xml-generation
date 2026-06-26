package com.company.xmlgen.masterdata.repository;

import com.company.xmlgen.masterdata.entity.MasterDataTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link MasterDataTypeEntity}.
 *
 * @see docs/11-implementation-guide/master-data.md
 */
public interface MasterDataTypeRepository extends JpaRepository<MasterDataTypeEntity, Long> {

    Page<MasterDataTypeEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByCode(String code);
}
