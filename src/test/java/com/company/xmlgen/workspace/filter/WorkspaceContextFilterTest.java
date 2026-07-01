package com.company.xmlgen.workspace.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.xmlgen.exception.ErrorResponseWriter;
import com.company.xmlgen.workspace.context.WorkspaceContext;
import com.company.xmlgen.workspace.context.WorkspaceContextHeaders;
import com.company.xmlgen.workspace.context.WorkspaceContextHolder;
import com.company.xmlgen.workspace.exception.WorkspaceErrorCode;
import com.company.xmlgen.workspace.service.WorkspaceContextResolver;
import jakarta.servlet.FilterChain;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class WorkspaceContextFilterTest {

    @Mock
    private WorkspaceContextResolver workspaceContextResolver;

    @Mock
    private ErrorResponseWriter errorResponseWriter;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private WorkspaceContextFilter workspaceContextFilter;

    @AfterEach
    void tearDown() {
        WorkspaceContextHolder.clear();
    }

    @Test
    void resolvesFromHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workspaces");
        request.addHeader(WorkspaceContextHeaders.WORKSPACE_ID, "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        WorkspaceContext context = new WorkspaceContext(1L, "DEFAULT");
        when(workspaceContextResolver.resolve(1L)).thenReturn(context);

        workspaceContextFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(errorResponseWriter, never()).writeError(any(), eq(HttpStatus.BAD_REQUEST.value()), any());
        assertThat(WorkspaceContextHolder.get()).isEmpty();
    }

    @Test
    void resolvesFromQueryParameterWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/templates");
        request.setParameter("workspaceId", "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(workspaceContextResolver.resolve(1L)).thenReturn(new WorkspaceContext(1L, "DEFAULT"));

        workspaceContextFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void headerTakesPrecedenceOverQueryParameter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/templates");
        request.addHeader(WorkspaceContextHeaders.WORKSPACE_ID, "2");
        request.setParameter("workspaceId", "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(workspaceContextResolver.resolve(2L)).thenReturn(new WorkspaceContext(2L, "ACME"));

        workspaceContextFilter.doFilter(request, response, filterChain);

        verify(workspaceContextResolver).resolve(2L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void missingWorkspaceRejected() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workspaces");
        MockHttpServletResponse response = new MockHttpServletResponse();

        workspaceContextFilter.doFilter(request, response, filterChain);

        verify(errorResponseWriter)
                .writeError(response, HttpStatus.BAD_REQUEST.value(), WorkspaceErrorCode.WORKSPACE_REQUIRED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void skipsLoginEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        workspaceContextFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(workspaceContextResolver, never()).resolve(any());
    }

    @Test
    void setsContextDuringFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workspaces");
        request.addHeader(WorkspaceContextHeaders.WORKSPACE_ID, "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<WorkspaceContext> contextDuringChain = new AtomicReference<>();
        when(workspaceContextResolver.resolve(1L)).thenReturn(new WorkspaceContext(1L, "DEFAULT"));
        org.mockito.Mockito.doAnswer(invocation -> {
                    contextDuringChain.set(WorkspaceContextHolder.require());
                    return null;
                })
                .when(filterChain)
                .doFilter(request, response);

        workspaceContextFilter.doFilter(request, response, filterChain);

        assertThat(contextDuringChain.get()).isEqualTo(new WorkspaceContext(1L, "DEFAULT"));
        assertThat(WorkspaceContextHolder.get()).isEmpty();
    }

    @Test
    void noContextLeakageAcrossConcurrentRequests() throws Exception {
        when(workspaceContextResolver.resolve(1L)).thenReturn(new WorkspaceContext(1L, "DEFAULT"));
        when(workspaceContextResolver.resolve(2L)).thenReturn(new WorkspaceContext(2L, "ACME"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        AtomicReference<WorkspaceContext> contextOne = new AtomicReference<>();
        AtomicReference<WorkspaceContext> contextTwo = new AtomicReference<>();

        executor.submit(() -> {
            try {
                runConcurrentRequest("1", contextOne, ready, start, done);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        executor.submit(() -> {
            try {
                runConcurrentRequest("2", contextTwo, ready, start, done);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(contextOne.get()).isEqualTo(new WorkspaceContext(1L, "DEFAULT"));
        assertThat(contextTwo.get()).isEqualTo(new WorkspaceContext(2L, "ACME"));
        assertThat(WorkspaceContextHolder.get()).isEmpty();
    }

    private void runConcurrentRequest(
            String workspaceId,
            AtomicReference<WorkspaceContext> captured,
            CountDownLatch ready,
            CountDownLatch start,
            CountDownLatch done)
            throws Exception {
        ready.countDown();
        start.await(5, TimeUnit.SECONDS);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/workspaces");
        request.addHeader(WorkspaceContextHeaders.WORKSPACE_ID, workspaceId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> captured.set(WorkspaceContextHolder.require());

        workspaceContextFilter.doFilter(request, response, chain);
        done.countDown();
    }
}
