package com.company.xmlgen.authentication.dto.response;

import com.company.xmlgen.authentication.domain.SystemRole;

/**
 * Response returned by {@code PUT /api/v1/users/{id}}.
 */
public record UpdateUserResponse(Long id, String username, SystemRole role) {}
