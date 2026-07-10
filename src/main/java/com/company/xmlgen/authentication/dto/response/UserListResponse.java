package com.company.xmlgen.authentication.dto.response;

import com.company.xmlgen.authentication.domain.SystemRole;
import java.time.Instant;

/**
 * List item returned by {@code GET /api/v1/users}.
 */
public record UserListResponse(
        Long id, String username, SystemRole role, Instant createdAt, Instant updatedAt) {}
