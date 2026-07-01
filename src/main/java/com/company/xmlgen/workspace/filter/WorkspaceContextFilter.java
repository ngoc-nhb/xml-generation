package com.company.xmlgen.workspace.filter;

import com.company.xmlgen.exception.ApplicationException;
import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.exception.ConflictException;
import com.company.xmlgen.exception.ErrorResponseWriter;
import com.company.xmlgen.workspace.context.WorkspaceContext;
import com.company.xmlgen.workspace.context.WorkspaceContextHeaders;
import com.company.xmlgen.workspace.context.WorkspaceContextHolder;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.service.WorkspaceContextResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Resolves {@link WorkspaceContext} for every authenticated API request.
 *
 * <p>Resolution order: {@code X-Workspace-Id} header, then {@code workspaceId} query parameter.
 * Neither present yields {@code WORKSPACE_REQUIRED}. No silent fallback to the default workspace.
 */
@Component
public class WorkspaceContextFilter extends OncePerRequestFilter {

    private static final String WORKSPACE_ID_PARAM = "workspaceId";

    private final WorkspaceContextResolver workspaceContextResolver;
    private final ErrorResponseWriter errorResponseWriter;

    public WorkspaceContextFilter(
            WorkspaceContextResolver workspaceContextResolver, ErrorResponseWriter errorResponseWriter) {
        this.workspaceContextResolver = workspaceContextResolver;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !requiresWorkspaceContext(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Long workspaceId = extractWorkspaceId(request);
            if (workspaceId == null) {
                errorResponseWriter.writeError(
                        response, HttpStatus.BAD_REQUEST.value(), WorkspaceErrorCode.WORKSPACE_REQUIRED);
                return;
            }

            WorkspaceContext context = workspaceContextResolver.resolve(workspaceId);
            WorkspaceContextHolder.set(context);
            filterChain.doFilter(request, response);
        } catch (ApplicationException ex) {
            errorResponseWriter.writeError(response, statusFor(ex), ex.getErrorCode());
        } finally {
            WorkspaceContextHolder.clear();
        }
    }

    static boolean requiresWorkspaceContext(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/actuator")) {
            return false;
        }
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return false;
        }
        if (HttpMethod.POST.matches(request.getMethod()) && "/api/v1/auth/login".equals(path)) {
            return false;
        }

        return path.startsWith("/api/v1/");
    }

    private Long extractWorkspaceId(HttpServletRequest request) {
        String header = request.getHeader(WorkspaceContextHeaders.WORKSPACE_ID);
        if (header != null && !header.isBlank()) {
            return parseWorkspaceId(header.trim());
        }

        String queryParam = request.getParameter(WORKSPACE_ID_PARAM);
        if (queryParam != null && !queryParam.isBlank()) {
            return parseWorkspaceId(queryParam.trim());
        }

        return null;
    }

    private Long parseWorkspaceId(String value) {
        try {
            long workspaceId = Long.parseLong(value);
            if (workspaceId <= 0) {
                throw new BusinessException(WorkspaceErrorCode.INVALID_WORKSPACE);
            }
            return workspaceId;
        } catch (NumberFormatException ex) {
            throw new BusinessException(WorkspaceErrorCode.INVALID_WORKSPACE);
        }
    }

    private static int statusFor(ApplicationException ex) {
        if (ex instanceof ConflictException) {
            return HttpStatus.CONFLICT.value();
        }
        return HttpStatus.BAD_REQUEST.value();
    }
}
