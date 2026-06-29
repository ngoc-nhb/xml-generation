package com.company.xmlgen.template.repository;

import com.company.xmlgen.template.entity.TemplateFieldEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link TemplateFieldEntity}.
 *
 * @see docs/08-class-diagram/p3_repository-layer.md §25
 */
public interface TemplateFieldRepository extends JpaRepository<TemplateFieldEntity, Long> {

    List<TemplateFieldEntity> findAllByTemplateIdOrderByDisplayOrderAsc(Long templateId);

    List<TemplateFieldEntity> findByTemplateId(Long templateId);

    long countByTemplateId(Long templateId);

    void deleteByTemplateId(Long templateId);

    boolean existsByTemplateIdAndParentIdAndFieldName(Long templateId, Long parentId, String fieldName);

    boolean existsByTemplateIdAndParentIdAndDisplayOrder(Long templateId, Long parentId, int displayOrder);
}
