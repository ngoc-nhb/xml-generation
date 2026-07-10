package com.company.xmlgen.authentication.dto.response;

import com.company.xmlgen.authentication.domain.SystemRole;

/**
 * Response returned by {@code POST /api/v1/users}.
 */
public record CreateUserResponse(Long id, String username, SystemRole role) {}
