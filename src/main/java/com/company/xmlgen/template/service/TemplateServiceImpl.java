package com.company.xmlgen.template.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
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

    private AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
