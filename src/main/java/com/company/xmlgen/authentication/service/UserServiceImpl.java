package com.company.xmlgen.authentication.service;

import com.company.xmlgen.authentication.dto.request.CreateUserRequest;
import com.company.xmlgen.authentication.dto.request.ResetPasswordRequest;
import com.company.xmlgen.authentication.dto.request.UpdateUserRequest;
import com.company.xmlgen.authentication.dto.response.CreateUserResponse;
import com.company.xmlgen.authentication.dto.response.UpdateUserResponse;
import com.company.xmlgen.authentication.dto.response.UserListResponse;
import com.company.xmlgen.authentication.dto.response.UserResponse;
import com.company.xmlgen.authentication.entity.UserEntity;
import com.company.xmlgen.authentication.exception.UserErrorCode;
import com.company.xmlgen.authentication.mapper.UserMapper;
import com.company.xmlgen.authentication.repository.UserRepository;
import com.company.xmlgen.common.api.PageMeta;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.NotFoundException;
import com.company.xmlgen.exception.ValidationException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages system user administration business rules.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuthorizationGuard adminAuthorizationGuard;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AdminAuthorizationGuard adminAuthorizationGuard) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.adminAuthorizationGuard = adminAuthorizationGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserListResponse> findAll(int page, int pageSize) {
        adminAuthorizationGuard.requireAdmin();

        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        Pageable pageable =
                PageRequest.of(normalizedPage - 1, normalizedPageSize, Sort.by("id").ascending());

        Page<UserEntity> entityPage = userRepository.findByDeletedAtIsNull(pageable);

        List<UserListResponse> content = entityPage.getContent().stream()
                .map(userMapper::toListResponse)
                .toList();

        PageMeta meta = new PageMeta(
                normalizedPage,
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages());

        return new PageResult<>(content, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        adminAuthorizationGuard.requireAdmin();
        return userMapper.toResponse(findActiveUser(id));
    }

    @Override
    @Transactional
    public CreateUserResponse create(CreateUserRequest request) {
        adminAuthorizationGuard.requireAdmin();

        String username = request.username().trim();
        validateUsernameUnique(username, null);

        UserEntity entity = new UserEntity(username, passwordEncoder.encode(request.password()), request.role().toAdminFlag());

        UserEntity saved = userRepository.save(entity);
        return userMapper.toCreateResponse(saved);
    }

    @Override
    @Transactional
    public UpdateUserResponse update(Long id, UpdateUserRequest request) {
        adminAuthorizationGuard.requireAdmin();

        UserEntity entity = findActiveUser(id);
        String username = request.username().trim();
        validateUsernameUnique(username, id);

        entity.setUsername(username);
        entity.setAdmin(request.role().toAdminFlag());

        UserEntity saved = userRepository.save(entity);
        return userMapper.toUpdateResponse(saved);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        adminAuthorizationGuard.requireAdmin();

        if (!request.password().equals(request.confirmPassword())) {
            throw new ValidationException(UserErrorCode.PASSWORD_MISMATCH, "confirmPassword", "Passwords do not match.");
        }

        UserEntity entity = findActiveUser(id);
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(entity);
    }

    private UserEntity findActiveUser(Long id) {
        return userRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));
    }

    private void validateUsernameUnique(String username, Long excludeId) {
        boolean exists = excludeId == null
                ? userRepository.existsByUsernameAndDeletedAtIsNull(username)
                : userRepository.existsByUsernameAndDeletedAtIsNullAndIdNot(username, excludeId);
        if (exists) {
            throw new ConflictException(UserErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }
}
