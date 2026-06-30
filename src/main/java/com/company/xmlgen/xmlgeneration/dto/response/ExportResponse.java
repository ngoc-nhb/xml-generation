package com.company.xmlgen.xmlgeneration.dto.response;

/**
 * HTTP success payload for template export.
 *
 * @see docs/06-api-design/p3_template-api.md §26B
 */
public record ExportResponse(String xml) {
}
