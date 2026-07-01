package com.company.xmlgen.support;

import com.company.xmlgen.workspace.context.WorkspaceContext;
import com.company.xmlgen.workspace.context.WorkspaceContextHolder;

/**
 * Test helper for setting the active workspace context in service-layer tests.
 */
public final class WorkspaceTestSupport {

    public static final WorkspaceContext DEFAULT_WORKSPACE = new WorkspaceContext(1L, "DEFAULT");

    private WorkspaceTestSupport() {}

    public static void useDefaultWorkspace() {
        WorkspaceContextHolder.set(DEFAULT_WORKSPACE);
    }

    public static void useWorkspace(long workspaceId, String workspaceCode) {
        WorkspaceContextHolder.set(new WorkspaceContext(workspaceId, workspaceCode));
    }

    public static void clearWorkspace() {
        WorkspaceContextHolder.clear();
    }
}
