package com.company.xmlgen.masterdata.repository;

import com.company.xmlgen.masterdata.entity.MasterDataRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for {@link MasterDataRecordEntity}.
 */
public interface MasterDataRecordRepository extends JpaRepository<MasterDataRecordEntity, Long> {

    @Query(
            value =
                    """
                    SELECT r.* FROM master_data_records r
                    WHERE r.master_data_type_id = :typeId
                      AND r.deleted_at IS NULL
                      AND (CAST(:keyword AS TEXT) IS NULL
                           OR LOWER(CAST(r.data_json AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:keyword AS TEXT), '%')))
                    """,
            countQuery =
                    """
                    SELECT COUNT(*) FROM master_data_records r
                    WHERE r.master_data_type_id = :typeId
                      AND r.deleted_at IS NULL
                      AND (CAST(:keyword AS TEXT) IS NULL
                           OR LOWER(CAST(r.data_json AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:keyword AS TEXT), '%')))
                    """,
            nativeQuery = true)
    Page<MasterDataRecordEntity> search(
            @Param("typeId") Long typeId, @Param("keyword") String keyword, Pageable pageable);
}
