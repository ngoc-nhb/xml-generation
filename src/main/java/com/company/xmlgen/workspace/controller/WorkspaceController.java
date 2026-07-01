package com.company.xmlgen.workspace.controller;

import com.company.xmlgen.common.api.ApiResponse;
import com.company.xmlgen.common.api.PageResult;
import com.company.xmlgen.workspace.dto.request.CreateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.request.UpdateWorkspaceRequest;
import com.company.xmlgen.workspace.dto.response.CreateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.MessageResponse;
import com.company.xmlgen.workspace.dto.response.UpdateWorkspaceResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceListResponse;
import com.company.xmlgen.workspace.dto.response.WorkspaceResponse;
import com.company.xmlgen.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workspace HTTP endpoints.
 *
 * @see docs/06-api-design/p9_workspace-api-strategy.md §4
 */
@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ApiResponse<List<WorkspaceListResponse>> findAll(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<WorkspaceListResponse> result = workspaceService.findAll(page, pageSize);
        return ApiResponse.ok(result.content(), result.meta());
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkspaceResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(workspaceService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateWorkspaceResponse> create(@Valid @RequestBody CreateWorkspaceRequest request) {
        return ApiResponse.ok(workspaceService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UpdateWorkspaceResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateWorkspaceRequest request) {
        return ApiResponse.ok(workspaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> delete(@PathVariable Long id) {
        workspaceService.delete(id);
        return ApiResponse.ok(new MessageResponse("Workspace deleted successfully."));
    }
}
