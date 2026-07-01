package com.company.xmlgen.workspace.context;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import java.util.Optional;

/**
 * Request-scoped holder for the active {@link WorkspaceContext}.
 *
 * <p>Populated by {@link com.company.xmlgen.workspace.filter.WorkspaceContextFilter} and cleared
 * after every request. Mirrors the {@code SecurityContextHolder} pattern.
 */
public final class WorkspaceContextHolder {

    private static final ThreadLocal<WorkspaceContext> CONTEXT = new ThreadLocal<>();

    private WorkspaceContextHolder() {}

    public static void set(WorkspaceContext context) {
        CONTEXT.set(context);
    }

    public static Optional<WorkspaceContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static WorkspaceContext require() {
        return get().orElseThrow(() -> new BusinessException(WorkspaceErrorCode.WORKSPACE_REQUIRED));
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
