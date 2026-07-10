package com.company.xmlgen.authentication.dto.request;

import com.company.xmlgen.authentication.domain.SystemRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/v1/users}.
 */
public record CreateUserRequest(
        @NotBlank String username, @NotBlank String password, @NotNull SystemRole role) {}
