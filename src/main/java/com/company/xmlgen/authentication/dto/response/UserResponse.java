package com.company.xmlgen.authentication.dto.response;

import com.company.xmlgen.authentication.domain.SystemRole;
import java.time.Instant;

/**
 * Detail response returned by {@code GET /api/v1/users/{id}}.
 */
public record UserResponse(
        Long id, String username, SystemRole role, Instant createdAt, Instant updatedAt) {}
