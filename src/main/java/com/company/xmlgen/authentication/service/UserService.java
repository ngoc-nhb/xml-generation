package com.company.xmlgen.authentication.service;

import com.company.xmlgen.authentication.dto.request.CreateUserRequest;
import com.company.xmlgen.authentication.dto.request.ResetPasswordRequest;
import com.company.xmlgen.authentication.dto.request.UpdateUserRequest;
import com.company.xmlgen.authentication.dto.response.CreateUserResponse;
import com.company.xmlgen.authentication.dto.response.UpdateUserResponse;
import com.company.xmlgen.authentication.dto.response.UserListResponse;
import com.company.xmlgen.authentication.dto.response.UserResponse;
import com.company.xmlgen.common.api.PageResult;

/**
 * User administration business operations.
 */
public interface UserService {

    PageResult<UserListResponse> findAll(int page, int pageSize);

    UserResponse findById(Long id);

    CreateUserResponse create(CreateUserRequest request);

    UpdateUserResponse update(Long id, UpdateUserRequest request);

    void resetPassword(Long id, ResetPasswordRequest request);
}
