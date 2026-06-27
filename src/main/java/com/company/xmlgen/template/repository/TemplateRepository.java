package com.company.xmlgen.template.repository;

import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence access for {@link TemplateEntity}.
 *
 * @see docs/11-implementation-guide/template.md §8
 * @see docs/08-class-diagram/p3_repository-layer.md §25
 */
public interface TemplateRepository extends JpaRepository<TemplateEntity, Long> {

    Optional<TemplateEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
            SELECT t FROM TemplateEntity t
            WHERE (:#{#keyword} IS NULL
                   OR LOWER(t.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:#{#status} IS NULL OR t.status = :status)
            """)
    Page<TemplateEntity> search(
            @Param("keyword") String keyword, @Param("status") TemplateStatus status, Pageable pageable);
}
