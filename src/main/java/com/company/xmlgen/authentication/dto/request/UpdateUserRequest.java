package com.company.xmlgen.authentication.dto.request;

import com.company.xmlgen.authentication.domain.SystemRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code PUT /api/v1/users/{id}}.
 */
public record UpdateUserRequest(@NotBlank String username, @NotNull SystemRole role) {}
