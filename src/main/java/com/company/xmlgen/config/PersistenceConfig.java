package com.company.xmlgen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class PersistenceConfig {

    /**
     * Stub until the Authentication module provides the current user id.
     * Returns empty so {@code @CreatedBy} / {@code @LastModifiedBy} are not populated yet.
     */
    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.empty();
    }
}
