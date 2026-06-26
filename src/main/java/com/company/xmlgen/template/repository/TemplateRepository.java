package com.company.xmlgen.template.repository;

import com.company.xmlgen.template.entity.TemplateEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link TemplateEntity}.
 *
 * @see docs/11-implementation-guide/template.md §8
 * @see docs/08-class-diagram/p3_repository-layer.md §25
 */
public interface TemplateRepository extends JpaRepository<TemplateEntity, Long> {

    Optional<TemplateEntity> findByCode(String code);

    boolean existsByCode(String code);

    Page<TemplateEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
