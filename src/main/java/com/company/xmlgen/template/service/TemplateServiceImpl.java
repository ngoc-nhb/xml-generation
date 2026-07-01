package com.company.xmlgen.template.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.FieldViolation;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.exception.ValidationException;
import com.company.xmlgen.template.dto.request.CreateTemplateFieldRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateMappingRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.CreateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateSchemaRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateListResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateFieldResponse;
import com.company.xmlgen.template.dto.response.TemplateMappingResponse;
import com.company.xmlgen.template.dto.response.TemplateSchemaResponse;
import com.company.xmlgen.template.dto.response.UpdateTemplateResponse;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateFieldEntity;
import com.company.xmlgen.template.entity.TemplateMappingEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateFieldRepository;
import com.company.xmlgen.template.repository.TemplateMappingRepository;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.workspace.service.WorkspaceOwnershipGuard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages template lifecycle business rules.
 *
 * @see docs/11-implementation-guide/template.md §4
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final TemplateRepository templateRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final TemplateMappingRepository templateMappingRepository;
    private final TemplateCompilationOrchestrator templateCompilationOrchestrator;
    private final WorkspaceOwnershipGuard workspaceOwnershipGuard;

    public TemplateServiceImpl(
            TemplateRepository templateRepository,
            TemplateFieldRepository templateFieldRepository,
            TemplateMappingRepository templateMappingRepository,
            TemplateCompilationOrchestrator templateCompilationOrchestrator,
            WorkspaceOwnershipGuard workspaceOwnershipGuard) {
        this.templateRepository = templateRepository;
        this.templateFieldRepository = templateFieldRepository;
        this.templateMappingRepository = templateMappingRepository;
        this.templateCompilationOrchestrator = templateCompilationOrchestrator;
        this.workspaceOwnershipGuard = workspaceOwnershipGuard;
    }

    @Override
    @Transactional
    public CreateTemplateResponse create(CreateTemplateRequest request) {
        AuthenticatedUser currentUser = getCurrentUser();
        long workspaceId = workspaceOwnershipGuard.currentWorkspaceId();

        if (templateRepository.existsByWorkspaceIdAndCode(workspaceId, request.code())) {
            throw new ConflictException(TemplateErrorCode.TEMPLATE_CODE_ALREADY_EXISTS);
        }

        TemplateEntity template = new TemplateEntity(
                request.code(),
                request.name(),
                TemplateStatus.ACTIVE,
                currentUser.id());
        template.setWorkspaceId(workspaceId);
        template.setDescription(request.description());

        TemplateEntity saved = templateRepository.save(template);

        if (request.schema() != null) {
            List<CreateTemplateFieldRequest> fields =
                    request.schema().fields() != null ? request.schema().fields() : List.of();
            List<CreateTemplateMappingRequest> mappings =
                    request.schema().mappings() != null ? request.schema().mappings() : List.of();
            replaceSchemaMetadata(saved.getId(), fields, mappings);
            templateCompilationOrchestrator.compileAndPersist(saved.getId());
        }

        return new CreateTemplateResponse(saved.getId());
    }

    @Override
    @Transactional
    public UpdateTemplateResponse update(Long id, UpdateTemplateRequest request) {
        TemplateEntity template = workspaceOwnershipGuard.requireTemplate(id);

        template.setName(request.name());
        template.setDescription(request.description());
        template.setStatus(request.status());

        TemplateEntity saved = templateRepository.save(template);

        return new UpdateTemplateResponse(
                saved.getId(),
                saved.getCode(),
                saved.getName(),
                saved.getDescription(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TemplateEntity template = workspaceOwnershipGuard.requireTemplate(id);

        templateRepository.delete(template);
        templateRepository.flush();
    }

    @Override
    @Transactional
    public TemplateSchemaResponse updateSchema(Long id, UpdateTemplateSchemaRequest request) {
        workspaceOwnershipGuard.requireTemplate(id);

        replaceSchemaMetadata(id, request.fields(), request.mappings());
        templateCompilationOrchestrator.compileAndPersist(id);

        return loadSchemaFromMetadata(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse findById(Long id) {
        TemplateEntity template = workspaceOwnershipGuard.requireTemplate(id);

        return new TemplateResponse(
                template.getId(),
                template.getCode(),
                template.getName(),
                template.getDescription(),
                template.getStatus(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                loadSchemaFromMetadata(template.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TemplateListResponse> findAll(
            int page, int pageSize, String keyword, TemplateStatus status) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Pageable pageable =
                PageRequest.of(normalizedPage - 1, normalizedPageSize, Sort.by("id").ascending());

        String normalizedKeyword = isBlank(keyword) ? null : keyword.trim();
        Page<TemplateEntity> entityPage = templateRepository.searchByWorkspace(
                workspaceOwnershipGuard.currentWorkspaceId(), normalizedKeyword, status, pageable);

        List<TemplateListResponse> content = entityPage.getContent().stream()
                .map(entity -> new TemplateListResponse(
                        entity.getId(),
                        entity.getCode(),
                        entity.getName(),
                        entity.getDescription(),
                        entity.getStatus(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()))
                .toList();

        PageMeta meta = new PageMeta(
                normalizedPage,
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages());

        return new PageResult<>(content, meta);
    }

    private void replaceSchemaMetadata(
            Long templateId,
            List<CreateTemplateFieldRequest> fields,
            List<CreateTemplateMappingRequest> mappings) {
        validateSchemaMetadata(fields, mappings);

        templateMappingRepository.deleteByTemplateId(templateId);
        templateFieldRepository.deleteByTemplateId(templateId);

        if (fields.isEmpty()) {
            return;
        }

        Map<String, Long> fieldNameToId = new HashMap<>();

        for (CreateTemplateFieldRequest field : orderFieldsByHierarchy(fields)) {
            TemplateFieldEntity entity = toFieldEntity(templateId, field);
            if (!isBlank(field.parentFieldName())) {
                entity.setParentId(fieldNameToId.get(field.parentFieldName()));
            }
            TemplateFieldEntity saved = templateFieldRepository.save(entity);
            fieldNameToId.put(field.fieldName(), saved.getId());
        }

        for (CreateTemplateMappingRequest mapping : mappings) {
            workspaceOwnershipGuard.requireMasterDataField(mapping.masterDataFieldId());
            TemplateMappingEntity mappingEntity = new TemplateMappingEntity(
                    templateId,
                    fieldNameToId.get(mapping.fieldName()),
                    mapping.masterDataFieldId());
            templateMappingRepository.save(mappingEntity);
        }
    }

    private void validateSchemaMetadata(
            List<CreateTemplateFieldRequest> fields, List<CreateTemplateMappingRequest> mappings) {
        List<FieldViolation> violations = new ArrayList<>();
        Set<String> fieldNames = new HashSet<>();
        Map<String, String> parentByFieldName = new HashMap<>();

        for (CreateTemplateFieldRequest field : fields) {
            if (!fieldNames.add(field.fieldName())) {
                violations.add(FieldViolation.of(
                        "schema.fields",
                        TemplateErrorCode.TEMPLATE_FIELD_NAME_DUPLICATE.code(),
                        "Duplicate fieldName: " + field.fieldName()));
            }
        }

        for (CreateTemplateFieldRequest field : fields) {
            if (isBlank(field.parentFieldName())) {
                continue;
            }
            if (field.fieldName().equals(field.parentFieldName())) {
                violations.add(FieldViolation.of(
                        "schema.fields",
                        TemplateErrorCode.TEMPLATE_INVALID_HIERARCHY.code(),
                        "Field cannot be its own parent: " + field.fieldName()));
                continue;
            }
            if (!fieldNames.contains(field.parentFieldName())) {
                violations.add(FieldViolation.of(
                        "schema.fields",
                        TemplateErrorCode.TEMPLATE_PARENT_FIELD_NOT_FOUND.code(),
                        "Unknown parentFieldName: " + field.parentFieldName()));
                continue;
            }
            parentByFieldName.put(field.fieldName(), field.parentFieldName());
        }

        for (CreateTemplateFieldRequest field : fields) {
            if (hasParentCycle(field.fieldName(), parentByFieldName, new HashSet<>())) {
                violations.add(FieldViolation.of(
                        "schema.fields",
                        TemplateErrorCode.TEMPLATE_PARENT_CYCLE.code(),
                        "Cyclic parent relationship detected for field: " + field.fieldName()));
            }
        }

        Map<String, Set<Integer>> displayOrdersByParent = new HashMap<>();
        for (CreateTemplateFieldRequest field : fields) {
            String parentKey = isBlank(field.parentFieldName()) ? "" : field.parentFieldName();
            Set<Integer> orders = displayOrdersByParent.computeIfAbsent(parentKey, key -> new HashSet<>());
            if (!orders.add(field.displayOrder())) {
                violations.add(FieldViolation.of(
                        "schema.fields",
                        TemplateErrorCode.TEMPLATE_DISPLAY_ORDER_DUPLICATE.code(),
                        "Duplicate displayOrder under the same parent: "
                                + (parentKey.isEmpty() ? "(root)" : parentKey)
                                + ", displayOrder="
                                + field.displayOrder()));
            }
        }

        if (!mappings.isEmpty() && fields.isEmpty()) {
            violations.add(FieldViolation.of(
                    "schema.mappings",
                    TemplateErrorCode.TEMPLATE_FIELD_NOT_FOUND.code(),
                    "Mappings require at least one field"));
        }

        Set<String> mappedFieldNames = new HashSet<>();
        for (CreateTemplateMappingRequest mapping : mappings) {
            if (!fieldNames.contains(mapping.fieldName())) {
                violations.add(FieldViolation.of(
                        "schema.mappings",
                        TemplateErrorCode.TEMPLATE_FIELD_NOT_FOUND.code(),
                        "Unknown fieldName: " + mapping.fieldName()));
                continue;
            }
            if (!mappedFieldNames.add(mapping.fieldName())) {
                violations.add(FieldViolation.of(
                        "schema.mappings",
                        TemplateErrorCode.TEMPLATE_MAPPING_DUPLICATE.code(),
                        "Duplicate mapping for fieldName: " + mapping.fieldName()));
            }
        }

        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    private static boolean hasParentCycle(
            String fieldName, Map<String, String> parentByFieldName, Set<String> visiting) {
        String parent = parentByFieldName.get(fieldName);
        if (parent == null) {
            return false;
        }
        if (!visiting.add(fieldName)) {
            return true;
        }
        return hasParentCycle(parent, parentByFieldName, visiting);
    }

    /**
     * Persists parents before children so {@code parent_id} is set on insert.
     *
     * <p>Without this ordering, all rows initially share {@code parent_id = null} and duplicate
     * {@code display_order} values under different parents violate
     * {@code uk_template_fields_template_parent_display_order}.
     */
    private static List<CreateTemplateFieldRequest> orderFieldsByHierarchy(
            List<CreateTemplateFieldRequest> fields) {
        List<CreateTemplateFieldRequest> ordered = new ArrayList<>();
        Set<String> persisted = new HashSet<>();

        while (ordered.size() < fields.size()) {
            boolean progress = false;
            for (CreateTemplateFieldRequest field : fields) {
                if (persisted.contains(field.fieldName())) {
                    continue;
                }
                if (isBlank(field.parentFieldName()) || persisted.contains(field.parentFieldName())) {
                    ordered.add(field);
                    persisted.add(field.fieldName());
                    progress = true;
                }
            }
            if (!progress) {
                throw new IllegalStateException("Unable to order template fields by hierarchy");
            }
        }
        return ordered;
    }

    private static TemplateFieldEntity toFieldEntity(Long templateId, CreateTemplateFieldRequest field) {
        TemplateFieldEntity entity = new TemplateFieldEntity(
                templateId,
                field.fieldName(),
                field.xmlName(),
                field.nodeType(),
                field.emptyHandling(),
                field.displayOrder());
        entity.setDisplayName(field.displayName());
        entity.setValueType(field.valueType());
        entity.setSourceType(field.sourceType());
        entity.setOccurrenceRule(field.occurrenceRule());
        if (field.requiredWhenParentExists() != null) {
            entity.setRequiredWhenParentExists(field.requiredWhenParentExists());
        }
        entity.setTriggerActivation(field.triggerActivation());
        entity.setDefaultValue(field.defaultValue());
        entity.setStaticValue(field.staticValue());
        entity.setXmlPath(field.xmlPath());
        entity.setNamespace(field.namespace());
        entity.setDescription(field.description());
        return entity;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private TemplateSchemaResponse loadSchemaFromMetadata(Long templateId) {
        if (templateFieldRepository.countByTemplateId(templateId) == 0
                && templateMappingRepository.countByTemplateId(templateId) == 0) {
            return null;
        }

        List<TemplateFieldEntity> fields =
                templateFieldRepository.findAllByTemplateIdOrderByDisplayOrderAsc(templateId);
        List<TemplateMappingEntity> mappings = templateMappingRepository.findAllByTemplateId(templateId);

        Map<Long, String> fieldIdToName = new HashMap<>();
        for (TemplateFieldEntity field : fields) {
            fieldIdToName.put(field.getId(), field.getFieldName());
        }

        List<TemplateFieldResponse> fieldResponses = fields.stream()
                .map(field -> toFieldResponse(field, fieldIdToName))
                .toList();

        List<TemplateMappingResponse> mappingResponses = mappings.stream()
                .map(mapping -> new TemplateMappingResponse(
                        fieldIdToName.get(mapping.getTemplateFieldId()),
                        mapping.getMasterDataFieldId()))
                .toList();

        return new TemplateSchemaResponse(null, fieldResponses, mappingResponses);
    }

    private static TemplateFieldResponse toFieldResponse(
            TemplateFieldEntity field, Map<Long, String> fieldIdToName) {
        String parentFieldName =
                field.getParentId() == null ? null : fieldIdToName.get(field.getParentId());
        return new TemplateFieldResponse(
                field.getFieldName(),
                parentFieldName,
                field.getXmlName(),
                field.getDisplayName(),
                field.getNodeType(),
                field.getValueType(),
                field.getSourceType(),
                field.getOccurrenceRule(),
                field.getEmptyHandling(),
                field.isRequiredWhenParentExists(),
                field.getTriggerActivation(),
                field.getDefaultValue(),
                field.getStaticValue(),
                field.getXmlPath(),
                field.getNamespace(),
                field.getDisplayOrder(),
                field.getDescription());
    }

    private AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
