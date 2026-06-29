package com.company.xmlgen.masterdata.repository;

import com.company.xmlgen.masterdata.entity.MasterDataFieldEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link MasterDataFieldEntity}.
 */
public interface MasterDataFieldRepository extends JpaRepository<MasterDataFieldEntity, Long> {

    Page<MasterDataFieldEntity> findByMasterDataTypeId(Long masterDataTypeId, Pageable pageable);

    List<MasterDataFieldEntity> findAllByMasterDataTypeId(Long masterDataTypeId);

    Page<MasterDataFieldEntity> findByMasterDataTypeIdAndNameContainingIgnoreCase(
            Long masterDataTypeId, String name, Pageable pageable);

    Page<MasterDataFieldEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByMasterDataTypeIdAndFieldName(Long masterDataTypeId, String fieldName);

    boolean existsByMasterDataTypeIdAndDisplayOrder(Long masterDataTypeId, int displayOrder);

    boolean existsByMasterDataTypeIdAndDisplayOrderAndIdNot(
            Long masterDataTypeId, int displayOrder, Long id);
}
