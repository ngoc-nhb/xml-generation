package com.company.xmlgen.workspace.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.xmlgen.exception.BusinessException;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WorkspaceContextHolderTest {

    @AfterEach
    void tearDown() {
        WorkspaceContextHolder.clear();
    }

    @Test
    void setAndGet() {
        WorkspaceContext context = new WorkspaceContext(1L, "DEFAULT");
        WorkspaceContextHolder.set(context);

        assertThat(WorkspaceContextHolder.get()).contains(context);
    }

    @Test
    void require_whenSet() {
        WorkspaceContext context = new WorkspaceContext(2L, "ACME");
        WorkspaceContextHolder.set(context);

        assertThat(WorkspaceContextHolder.require()).isEqualTo(context);
    }

    @Test
    void require_whenMissing() {
        assertThatThrownBy(WorkspaceContextHolder::require)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(WorkspaceErrorCode.WORKSPACE_REQUIRED);
    }

    @Test
    void clear_removesContext() {
        WorkspaceContextHolder.set(new WorkspaceContext(1L, "DEFAULT"));
        WorkspaceContextHolder.clear();

        assertThat(WorkspaceContextHolder.get()).isEmpty();
    }

    @Test
    void noLeakageBetweenSequentialRequests() {
        WorkspaceContextHolder.set(new WorkspaceContext(1L, "DEFAULT"));
        WorkspaceContextHolder.clear();

        assertThat(WorkspaceContextHolder.get()).isEmpty();
    }
}
