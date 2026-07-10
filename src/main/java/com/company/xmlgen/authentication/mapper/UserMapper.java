package com.company.xmlgen.authentication.mapper;

import com.company.xmlgen.authentication.domain.SystemRole;
import com.company.xmlgen.authentication.dto.response.CreateUserResponse;
import com.company.xmlgen.authentication.dto.response.UpdateUserResponse;
import com.company.xmlgen.authentication.dto.response.UserListResponse;
import com.company.xmlgen.authentication.dto.response.UserResponse;
import com.company.xmlgen.authentication.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Maps {@link UserEntity} to user management API response DTOs.
 */
@Component
public class UserMapper {

    public UserListResponse toListResponse(UserEntity entity) {
        return new UserListResponse(
                entity.getId(),
                entity.getUsername(),
                SystemRole.fromAdminFlag(entity.isAdmin()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                SystemRole.fromAdminFlag(entity.isAdmin()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public CreateUserResponse toCreateResponse(UserEntity entity) {
        return new CreateUserResponse(
                entity.getId(), entity.getUsername(), SystemRole.fromAdminFlag(entity.isAdmin()));
    }

    public UpdateUserResponse toUpdateResponse(UserEntity entity) {
        return new UpdateUserResponse(
                entity.getId(), entity.getUsername(), SystemRole.fromAdminFlag(entity.isAdmin()));
    }
}
