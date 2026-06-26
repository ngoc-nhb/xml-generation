package com.company.xmlgen.authentication.dto.response;

/**
 * Response {@code data} payload for {@code POST /api/v1/auth/login}.
 *
 * @see docs/06-api-design/p2_authen-api.md §15
 */
public record LoginResponse(Long userId, String username, boolean isAdmin, String accessToken) {
}
