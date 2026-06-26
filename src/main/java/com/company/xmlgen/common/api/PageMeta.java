package com.company.xmlgen.common.api;

/**
 * Pagination metadata included in paginated responses.
 *
 * @see <a href="file:docs/06-api-design/p8_error-model.md">docs/06-api-design/p8_error-model.md</a>
 */
public record PageMeta(
        int page,
        int pageSize,
        long totalRecords,
        int totalPages) {
}
