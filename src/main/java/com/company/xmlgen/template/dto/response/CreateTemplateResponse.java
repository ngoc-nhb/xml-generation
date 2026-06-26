package com.company.xmlgen.template.dto.response;

/**
 * Response {@code data} payload for {@code POST /api/v1/templates}.
 *
 * @see docs/06-api-design/p3_template-api.md §24
 */
public record CreateTemplateResponse(Long id) {
}
