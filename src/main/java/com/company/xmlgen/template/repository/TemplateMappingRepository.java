package com.company.xmlgen.template.repository;

import com.company.xmlgen.template.entity.TemplateMappingEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link TemplateMappingEntity}.
 *
 * @see docs/08-class-diagram/p3_repository-layer.md §25
 */
public interface TemplateMappingRepository extends JpaRepository<TemplateMappingEntity, Long> {

    List<TemplateMappingEntity> findAllByTemplateId(Long templateId);

    Optional<TemplateMappingEntity> findByTemplateFieldId(Long templateFieldId);

    long countByTemplateId(Long templateId);

    void deleteByTemplateId(Long templateId);

    boolean existsByTemplateFieldId(Long templateFieldId);
}
