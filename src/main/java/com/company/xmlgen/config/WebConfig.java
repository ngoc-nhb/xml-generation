package com.company.xmlgen.config;

import com.company.xmlgen.template.entity.TemplateStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web-layer customizations (formatters, interceptors, message converters).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, TemplateStatus.class, TemplateStatus::fromValue);
    }
}
