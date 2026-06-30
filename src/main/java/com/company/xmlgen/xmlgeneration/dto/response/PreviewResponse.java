package com.company.xmlgen.xmlgeneration.dto.response;

/**
 * HTTP success payload for template preview.
 *
 * @see docs/06-api-design/p3_template-api.md §26A
 */
public record PreviewResponse(String xml) {
}
