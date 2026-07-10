package com.company.xmlgen.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code PUT /api/v1/users/{id}/password}.
 */
public record ResetPasswordRequest(@NotBlank String password, @NotBlank String confirmPassword) {}
