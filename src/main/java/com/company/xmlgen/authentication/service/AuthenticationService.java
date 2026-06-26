package com.company.xmlgen.authentication.service;

import com.company.xmlgen.authentication.dto.request.LoginRequest;
import com.company.xmlgen.authentication.dto.response.LoginResponse;

/**
 * Authentication use cases.
 *
 * @see docs/11-implementation-guide/authentication.md §6
 */
public interface AuthenticationService {

    LoginResponse login(LoginRequest request);
}
