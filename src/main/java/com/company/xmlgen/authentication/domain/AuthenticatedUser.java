package com.company.xmlgen.authentication.domain;

/**
 * Identity of the currently authenticated user, stored as the SecurityContext principal.
 *
 * @see docs/11-implementation-guide/authentication.md §7
 */
public record AuthenticatedUser(Long id, String username, boolean admin) {
}
