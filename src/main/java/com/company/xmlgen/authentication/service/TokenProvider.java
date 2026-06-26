package com.company.xmlgen.authentication.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;

/**
 * Generates and validates authentication tokens.
 *
 * @see docs/11-implementation-guide/authentication.md §6
 */
public interface TokenProvider {

    String generate(AuthenticatedUser authenticatedUser);

    AuthenticatedUser resolveAuthenticatedUser(String token);
}
