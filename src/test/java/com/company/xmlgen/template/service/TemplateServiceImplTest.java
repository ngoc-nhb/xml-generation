package com.company.xmlgen.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.dto.response.TemplateListResponse;
import com.company.xmlgen.template.dto.response.TemplateResponse;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TemplateServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String TEMPLATE_CODE = "LIVE_GAME";
    private static final String TEMPLATE_NAME = "Live Game";
    private static final String DESCRIPTION = "J League Live Match XML";

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private TemplateServiceImpl templateService;

    @BeforeEach
    void setUp() {
        AuthenticatedUser currentUser = new AuthenticatedUser(USER_ID, "admin", true);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_success() {
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION);
        when(templateRepository.existsByCode(TEMPLATE_CODE)).thenReturn(false);
        TemplateEntity persisted = mock(TemplateEntity.class);
        when(persisted.getId()).thenReturn(10L);
        when(templateRepository.save(any(TemplateEntity.class))).thenReturn(persisted);

        CreateTemplateResponse response = templateService.create(request);

        assertThat(response.id()).isEqualTo(10L);

        ArgumentCaptor<TemplateEntity> captor = ArgumentCaptor.forClass(TemplateEntity.class);
        verify(templateRepository).save(captor.capture());
        TemplateEntity saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo(TEMPLATE_CODE);
        assertThat(saved.getName()).isEqualTo(TEMPLATE_NAME);
        assertThat(saved.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(saved.getStatus()).isEqualTo(TemplateStatus.ACTIVE);
        assertThat(saved.getCompiledSchemaJson()).isNull();
        assertThat(saved.getCreatedById()).isEqualTo(USER_ID);
    }

    @Test
    void create_duplicateTemplateCode() {
        CreateTemplateRequest request =
                new CreateTemplateRequest(TEMPLATE_CODE, TEMPLATE_NAME, DESCRIPTION);
        when(templateRepository.existsByCode(TEMPLATE_CODE)).thenReturn(true);

        assertThatThrownBy(() -> templateService.create(request))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_CODE_ALREADY_EXISTS);

        verify(templateRepository, never()).save(any());
    }

    @Test
    void findById_success() {
        TemplateEntity entity = mock(TemplateEntity.class);
        when(entity.getId()).thenReturn(10L);
        when(entity.getCode()).thenReturn(TEMPLATE_CODE);
        when(entity.getName()).thenReturn(TEMPLATE_NAME);
        when(entity.getDescription()).thenReturn(DESCRIPTION);
        when(entity.getStatus()).thenReturn(TemplateStatus.ACTIVE);
        when(templateRepository.findById(10L)).thenReturn(Optional.of(entity));

        TemplateResponse response = templateService.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.templateCode()).isEqualTo(TEMPLATE_CODE);
        assertThat(response.templateName()).isEqualTo(TEMPLATE_NAME);
        assertThat(response.description()).isEqualTo(DESCRIPTION);
        assertThat(response.status()).isEqualTo(TemplateStatus.ACTIVE);
    }

    @Test
    void findById_notFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .extracting(ex -> ((NotFoundException) ex).getErrorCode())
                .isEqualTo(TemplateErrorCode.TEMPLATE_NOT_FOUND);
    }

    @Test
    void findAll_success() {
        TemplateEntity entity = mock(TemplateEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getCode()).thenReturn(TEMPLATE_CODE);
        when(entity.getName()).thenReturn(TEMPLATE_NAME);
        when(entity.getStatus()).thenReturn(TemplateStatus.ACTIVE);
        Page<TemplateEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);
        when(templateRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, null);

        assertThat(result.content()).hasSize(1);
        TemplateListResponse item = result.content().get(0);
        assertThat(item.id()).isEqualTo(1L);
        assertThat(item.templateCode()).isEqualTo(TEMPLATE_CODE);
        assertThat(item.templateName()).isEqualTo(TEMPLATE_NAME);
        assertThat(item.status()).isEqualTo(TemplateStatus.ACTIVE);
        assertThat(result.meta().page()).isEqualTo(1);
        assertThat(result.meta().pageSize()).isEqualTo(20);
        assertThat(result.meta().totalRecords()).isEqualTo(1);
        assertThat(result.meta().totalPages()).isEqualTo(1);
    }

    @Test
    void findAll_empty() {
        Page<TemplateEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(templateRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResult<TemplateListResponse> result = templateService.findAll(1, 20, "");

        assertThat(result.content()).isEmpty();
        assertThat(result.meta().totalRecords()).isZero();
        assertThat(result.meta().totalPages()).isZero();
    }

    @Test
    void findAll_withKeyword() {
        Page<TemplateEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(templateRepository.findByNameContainingIgnoreCase(eq("Live"), any(Pageable.class)))
                .thenReturn(page);

        templateService.findAll(1, 20, "  Live  ");

        verify(templateRepository).findByNameContainingIgnoreCase(eq("Live"), any(Pageable.class));
        verify(templateRepository, never()).findAll(any(Pageable.class));
    }
}
