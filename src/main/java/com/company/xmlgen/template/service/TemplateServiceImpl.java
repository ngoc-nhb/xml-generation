package com.company.xmlgen.template.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.request.UpdateTemplateRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateListResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
import java.util.List;
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

    public TemplateServiceImpl(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional
    public CreateTemplateResponse create(CreateTemplateRequest request) {
        AuthenticatedUser currentUser = getCurrentUser();

        if (templateRepository.existsByCode(request.templateCode())) {
            throw new ConflictException(TemplateErrorCode.TEMPLATE_CODE_ALREADY_EXISTS);
        }

        TemplateEntity template = new TemplateEntity(
                request.templateCode(),
                request.templateName(),
                TemplateStatus.ACTIVE,
                currentUser.id());
        template.setDescription(request.description());

        TemplateEntity saved = templateRepository.save(template);
        return new CreateTemplateResponse(saved.getId());
    }

    @Override
    @Transactional
    public TemplateResponse update(Long id, UpdateTemplateRequest request) {
        TemplateEntity template = templateRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        template.setName(request.templateName());
        template.setDescription(request.description());

        return new TemplateResponse(
                template.getId(),
                template.getCode(),
                template.getName(),
                template.getDescription(),
                template.getStatus());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TemplateEntity template = templateRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        templateRepository.delete(template);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse findById(Long id) {
        TemplateEntity template = templateRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND));

        return new TemplateResponse(
                template.getId(),
                template.getCode(),
                template.getName(),
                template.getDescription(),
                template.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TemplateListResponse> findAll(int page, int pageSize, String keyword) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Pageable pageable =
                PageRequest.of(normalizedPage - 1, normalizedPageSize, Sort.by("id").ascending());

        Page<TemplateEntity> entityPage = isBlank(keyword)
                ? templateRepository.findAll(pageable)
                : templateRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);

        List<TemplateListResponse> content = entityPage.getContent().stream()
                .map(entity -> new TemplateListResponse(
                        entity.getId(), entity.getCode(), entity.getName(), entity.getStatus()))
                .toList();

        PageMeta meta = new PageMeta(
                normalizedPage,
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages());

        return new PageResult<>(content, meta);
    }

    private static boolean isBlank(String keyword) {
        return keyword == null || keyword.isBlank();
    }

    private AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
