package com.company.xmlgen.template.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.dto.response.CreateTemplateResponse;
import com.company.xmlgen.template.entity.TemplateEntity;
import com.company.xmlgen.template.entity.TemplateStatus;
import com.company.xmlgen.template.exception.TemplateErrorCode;
import com.company.xmlgen.template.repository.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
}
