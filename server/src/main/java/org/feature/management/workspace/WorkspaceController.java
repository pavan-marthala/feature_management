package org.feature.management.workspace;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.feature.management.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceServiceInterface workspaceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> createWorkspace(@RequestBody @Valid WorkspaceRequest request) {
        log.debug("Controller: Creating workspace {}", request.getName());
        return workspaceService.createWorkspace(request);
    }

    @GetMapping
    public Mono<WorkspaceResponse> getWorkspaces(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer size) {
        log.debug("Controller: Getting all workspaces");
        return workspaceService.getWorkspaces(page, size);
    }

    @GetMapping("/{id}")
    public Mono<Workspace> getWorkspaceById(@PathVariable UUID id) {
        log.debug("Controller: Getting workspace by id {}", id);
        return workspaceService.getWorkspaceById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateWorkspace(
            @PathVariable UUID id,
            @RequestBody @Valid WorkspaceRequest request) {
        log.debug("Controller: Updating workspace {}", id);
        return workspaceService.updateWorkspace(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteWorkspace(@PathVariable UUID id) {
        log.debug("Controller: Deleting workspace {}", id);
        return workspaceService.deleteWorkspace(id);
    }

    @GetMapping("/{workspaceId}/summary")
    public Mono<GetWorkspaceSummary200Response> getWorkspaceSummary(@PathVariable UUID workspaceId) {
        log.debug("Controller: Getting workspace summary for {}", workspaceId);
        return workspaceService.getWorkspaceSummary(workspaceId);
    }

    @GetMapping("/{workspaceId}/features")
    public Mono<FeatureResponse> getWorkspaceFeatures(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) UUID environmentId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer size) {
        log.debug("Controller: Getting features for workspace {} and environment {}", workspaceId, environmentId);
        return workspaceService.getWorkspaceFeatures(workspaceId, environmentId, page, size);
    }
}
