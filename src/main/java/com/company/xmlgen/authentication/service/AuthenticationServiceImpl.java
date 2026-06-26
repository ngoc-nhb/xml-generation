package com.company.xmlgen.authentication.service;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.authentication.dto.request.LoginRequest;
import com.company.xmlgen.authentication.dto.response.LoginResponse;
import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.authentication.exception.AuthenticationErrorCode;
import com.company.xmlgen.authentication.repository.UserRepository;
import com.company.xmlgen.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticates users and issues JWT access tokens.
 *
 * @see docs/11-implementation-guide/authentication.md §4
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository
                .findByUsername(request.username())
                .orElseThrow(this::invalidCredentials);

        if (!user.isActive()) {
            throw invalidCredentials();
        }

        // TODO(authentication-phase-2): reject login when deleted_at is not null.

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(user.getId(), user.getUsername(), user.isAdmin());
        String accessToken = tokenProvider.generate(authenticatedUser);

        return new LoginResponse(user.getId(), user.getUsername(), user.isAdmin(), accessToken);
    }

    private UnauthorizedException invalidCredentials() {
        return new UnauthorizedException(AuthenticationErrorCode.INVALID_CREDENTIALS);
    }
}
