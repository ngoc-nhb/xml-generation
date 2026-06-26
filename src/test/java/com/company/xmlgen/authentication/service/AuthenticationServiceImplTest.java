package com.company.xmlgen.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.authentication.dto.request.LoginRequest;
import com.company.xmlgen.authentication.dto.response.LoginResponse;
import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.authentication.exception.AuthenticationErrorCode;
import com.company.xmlgen.authentication.repository.UserRepository;
import com.company.xmlgen.exception.UnauthorizedException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceImplTest {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";
    private static final String PASSWORD_HASH = "$2a$10$hash";
    private static final String ACCESS_TOKEN = "jwt-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private UserEntity activeUser;

    @BeforeEach
    void setUp() {
        activeUser = mock(UserEntity.class);
        when(activeUser.getId()).thenReturn(1L);
        when(activeUser.getUsername()).thenReturn(USERNAME);
        when(activeUser.getPasswordHash()).thenReturn(PASSWORD_HASH);
        when(activeUser.isAdmin()).thenReturn(true);
        when(activeUser.isActive()).thenReturn(true);
    }

    @Test
    void login_success() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(tokenProvider.generate(any(AuthenticatedUser.class))).thenReturn(ACCESS_TOKEN);

        LoginResponse response = authenticationService.login(new LoginRequest(USERNAME, PASSWORD));

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo(USERNAME);
        assertThat(response.isAdmin()).isTrue();
        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        verify(tokenProvider).generate(new AuthenticatedUser(1L, USERNAME, true));
    }

    @Test
    void login_invalidUsername() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest(USERNAME, PASSWORD)))
                .isInstanceOf(UnauthorizedException.class)
                .extracting(ex -> ((UnauthorizedException) ex).getErrorCode())
                .isEqualTo(AuthenticationErrorCode.INVALID_CREDENTIALS);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generate(any());
    }

    @Test
    void login_invalidPassword() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest(USERNAME, PASSWORD)))
                .isInstanceOf(UnauthorizedException.class)
                .extracting(ex -> ((UnauthorizedException) ex).getErrorCode())
                .isEqualTo(AuthenticationErrorCode.INVALID_CREDENTIALS);

        verify(tokenProvider, never()).generate(any());
    }

    @Test
    void login_inactiveUser() {
        when(activeUser.isActive()).thenReturn(false);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest(USERNAME, PASSWORD)))
                .isInstanceOf(UnauthorizedException.class)
                .extracting(ex -> ((UnauthorizedException) ex).getErrorCode())
                .isEqualTo(AuthenticationErrorCode.INVALID_CREDENTIALS);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generate(any());
    }

    @Test
    void login_generatesJwt() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(tokenProvider.generate(new AuthenticatedUser(1L, USERNAME, true))).thenReturn(ACCESS_TOKEN);

        LoginResponse response = authenticationService.login(new LoginRequest(USERNAME, PASSWORD));

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        verify(tokenProvider).generate(new AuthenticatedUser(1L, USERNAME, true));
    }
}
