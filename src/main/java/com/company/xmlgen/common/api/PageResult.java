package com.company.xmlgen.common.api;

import java.util.List;

/**
 * Service-layer paginated result carrying page content and metadata.
 *
 * @see <a href="file:docs/adr/ADR-001-service-layer-boundary.md">ADR-001</a>
 */
public record PageResult<T>(List<T> content, PageMeta meta) {
}
