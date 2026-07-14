package com.company.xmlgen.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.authentication.domain.AuthenticatedUser;
import com.company.xmlgen.authentication.domain.SystemRole;
import com.company.xmlgen.authentication.dto.request.CreateUserRequest;
import com.company.xmlgen.authentication.dto.request.ResetPasswordRequest;
import com.company.xmlgen.authentication.dto.request.UpdateUserRequest;
import com.company.xmlgen.authentication.dto.response.CreateUserResponse;
import com.company.xmlgen.authentication.dto.response.UpdateUserResponse;
import com.company.xmlgen.authentication.dto.response.UserListResponse;
import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.authentication.exception.UserErrorCode;
import com.company.xmlgen.authentication.mapper.UserMapper;
import com.company.xmlgen.authentication.repository.UserRepository;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.CommonErrorCode;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.ForbiddenException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.exception.ValidationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(1L, "admin", true);
    private static final AuthenticatedUser REGULAR = new AuthenticatedUser(2L, "user", false);

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminAuthorizationGuard adminAuthorizationGuard;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUpAdmin() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findAll_success() {
        UserEntity entity = userEntity(10L, "alice", false);
        Page<UserEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1);
        when(userRepository.findByDeletedAtIsNull(any(Pageable.class))).thenReturn(page);
        when(userMapper.toListResponse(entity))
                .thenReturn(new UserListResponse(
                        10L, "alice", SystemRole.USER, entity.getCreatedAt(), entity.getUpdatedAt()));

        PageResult<UserListResponse> result = userService.findAll(1, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().username()).isEqualTo("alice");
        verify(adminAuthorizationGuard).requireAdmin();
    }

    @Test
    void findAll_forbiddenForNonAdmin() {
        doThrow(new ForbiddenException(CommonErrorCode.FORBIDDEN)).when(adminAuthorizationGuard).requireAdmin();

        assertThatThrownBy(() -> userService.findAll(1, 20)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void create_success_encodesPasswordWithoutPersonalWorkspace() {
        CreateUserRequest request = new CreateUserRequest("newuser", "secret", SystemRole.USER);
        when(userRepository.existsByUsernameAndDeletedAtIsNull("newuser")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-hash");

        UserEntity saved = userEntity(5L, "newuser", false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(saved);
        when(userMapper.toCreateResponse(saved))
                .thenReturn(new CreateUserResponse(5L, "newuser", SystemRole.USER));

        CreateUserResponse response = userService.create(request);

        assertThat(response.username()).isEqualTo("newuser");
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void create_duplicateUsername() {
        CreateUserRequest request = new CreateUserRequest("existing", "secret", SystemRole.USER);
        when(userRepository.existsByUsernameAndDeletedAtIsNull("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class)
                .extracting(ex -> ((ConflictException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.USERNAME_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_success() {
        UserEntity entity = userEntity(3L, "oldname", false);
        when(userRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.of(entity));
        when(userRepository.existsByUsernameAndDeletedAtIsNullAndIdNot("newname", 3L)).thenReturn(false);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toUpdateResponse(entity))
                .thenReturn(new UpdateUserResponse(3L, "newname", SystemRole.ADMIN));

        UpdateUserResponse response = userService.update(3L, new UpdateUserRequest("newname", SystemRole.ADMIN));

        assertThat(response.username()).isEqualTo("newname");
        assertThat(entity.isAdmin()).isTrue();
    }

    @Test
    void update_notFound() {
        when(userRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, new UpdateUserRequest("name", SystemRole.USER)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void resetPassword_success() {
        UserEntity entity = userEntity(4L, "bob", false);
        when(userRepository.findByIdAndDeletedAtIsNull(4L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.encode("newpass")).thenReturn("new-hash");

        userService.resetPassword(4L, new ResetPasswordRequest("newpass", "newpass"));

        assertThat(entity.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(entity);
    }

    @Test
    void resetPassword_mismatch() {
        assertThatThrownBy(() -> userService.resetPassword(4L, new ResetPasswordRequest("a", "b")))
                .isInstanceOf(ValidationException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void nonAdminContext_stillRequiresGuard() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(REGULAR, null, List.of()));
        doThrow(new ForbiddenException(CommonErrorCode.FORBIDDEN)).when(adminAuthorizationGuard).requireAdmin();

        assertThatThrownBy(() -> userService.findById(1L)).isInstanceOf(ForbiddenException.class);
    }

    private static UserEntity userEntity(long id, String username, boolean admin) {
        UserEntity entity = new UserEntity(username, "hash", admin);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
