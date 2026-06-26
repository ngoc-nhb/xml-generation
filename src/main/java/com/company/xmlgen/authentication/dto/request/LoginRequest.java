package com.company.xmlgen.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/v1/auth/login}.
 *
 * @see docs/06-api-design/p2_authen-api.md §15
 */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
