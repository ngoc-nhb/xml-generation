package com.company.xmlgen.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.support.TestcontainersConfig;
import com.company.xmlgen.template.dto.request.CreateTemplateRequest;
import com.company.xmlgen.template.repository.TemplateRepository;
import com.company.xmlgen.template.service.TemplateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfig.class)
@EnabledIf("isDockerAvailable")
class TemplateDeleteIntegrationTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateRepository templateRepository;

    @BeforeEach
    void setUp() {
        AuthenticatedUser currentUser = new AuthenticatedUser(1L, "admin", true);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Test
    void createAfterDelete_allowsSameTemplateCode() {
        String code = "DELETE_INTEGRATION_TEST";
        CreateTemplateRequest request = new CreateTemplateRequest(code, "Original Name", "description", null);

        Long id = templateService.create(request).id();
        assertThat(templateRepository.findByCode(code)).isPresent();

        templateService.delete(id);
        assertThat(templateRepository.findById(id)).isEmpty();
        assertThat(templateRepository.findByCode(code)).isEmpty();

        Long recreatedId = templateService.create(request).id();
        assertThat(recreatedId).isNotEqualTo(id);
        assertThat(templateRepository.findByCode(code)).isPresent();
    }
}
