package com.company.xmlgen.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Standardized response envelope used by every API.
 *
 * @see <a href="file:docs/06-api-design/p1_overview.md">docs/06-api-design/p1_overview.md</a>
 * @see <a href="file:docs/06-api-design/p8_error-model.md">docs/06-api-design/p8_error-model.md</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        List<ApiError> errors,
        PageMeta meta) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, PageMeta meta) {
        return new ApiResponse<>(true, data, null, meta);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null, null);
    }

    public static ApiResponse<Void> failure(List<ApiError> errors) {
        return new ApiResponse<>(false, null, errors, null);
    }

    public static ApiResponse<Void> failure(ApiError error) {
        return new ApiResponse<>(false, null, List.of(error), null);
    }
}
