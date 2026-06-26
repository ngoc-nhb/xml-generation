package com.company.xmlgen.authentication.controller;

import com.company.xmlgen.authentication.dto.request.LoginRequest;
import com.company.xmlgen.authentication.dto.response.LoginResponse;
import com.company.xmlgen.authentication.service.AuthenticationService;
import com.company.xmlgen.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication HTTP endpoints.
 *
 * @see docs/06-api-design/p2_authen-api.md §15
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authenticationService.login(request));
    }
}
