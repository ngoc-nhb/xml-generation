package com.company.xmlgen.template.dto.request;

import com.company.xmlgen.template.entity.TemplateFieldEmptyHandling;
import com.company.xmlgen.template.entity.TemplateFieldNodeType;
import com.company.xmlgen.template.entity.TemplateFieldOccurrenceRule;
import com.company.xmlgen.template.entity.TemplateFieldSourceType;
import com.company.xmlgen.template.entity.TemplateFieldValueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Field metadata within a create-template schema payload.
 */
public record CreateTemplateFieldRequest(
        @NotBlank @Size(max = 255) String fieldName,
        @Size(max = 255) String parentFieldName,
        @NotBlank @Size(max = 255) String xmlName,
        @Size(max = 255) String displayName,
        @NotNull TemplateFieldNodeType nodeType,
        TemplateFieldValueType valueType,
        TemplateFieldSourceType sourceType,
        TemplateFieldOccurrenceRule occurrenceRule,
        @NotNull TemplateFieldEmptyHandling emptyHandling,
        Boolean requiredWhenParentExists,
        Boolean triggerActivation,
        String defaultValue,
        String staticValue,
        @Size(max = 1000) String xmlPath,
        @Size(max = 255) String namespace,
        @NotNull Integer displayOrder,
        String description) {
}
