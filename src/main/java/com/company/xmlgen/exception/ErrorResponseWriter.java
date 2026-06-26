package com.company.xmlgen.exception;

import com.company.xmlgen.common.api.ApiError;
import com.company.xmlgen.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Single place for constructing and serializing standardized error responses.
 * Used by {@link GlobalExceptionHandler} and Spring Security entry-point handlers.
 */
@Component
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public ErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ApiResponse<Void> failure(ErrorCode errorCode) {
        return ApiResponse.failure(ApiError.of(errorCode.code()));
    }

    public ApiResponse<Void> failure(String field, ErrorCode errorCode) {
        return ApiResponse.failure(ApiError.of(field, errorCode.code()));
    }

    public ApiResponse<Void> failure(List<ApiError> errors) {
        return ApiResponse.failure(errors);
    }

    public ApiResponse<Void> violationsToFailure(List<FieldViolation> violations) {
        List<ApiError> errors = violations.stream()
                .map(v -> ApiError.of(v.field(), v.code()))
                .toList();
        return ApiResponse.failure(errors);
    }

    public void writeError(HttpServletResponse response, int status, ErrorCode errorCode)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), failure(errorCode));
    }
}
