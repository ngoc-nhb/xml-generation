package com.company.xmlgen.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Machine-readable error object. The Backend never returns localized messages;
 * Frontend applications translate the code.
 *
 * @see <a href="file:docs/06-api-design/p8_error-model.md">docs/06-api-design/p8_error-model.md</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String field, String code) {

    public static ApiError of(String code) {
        return new ApiError(null, code);
    }

    public static ApiError of(String field, String code) {
        return new ApiError(field, code);
    }
}
