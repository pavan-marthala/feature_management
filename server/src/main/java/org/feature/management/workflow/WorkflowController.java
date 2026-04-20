package org.feature.management.workflow;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.feature.management.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public Mono<WorkflowResponse> getAllWorkflows(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "25") Integer size) {
        return workflowService.getAllWorkflows(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> createWorkflow(@Valid @RequestBody WorkflowBase workflowBase) {
        return workflowService.createWorkflow(workflowBase);
    }

    @GetMapping("/{id}")
    public Mono<WorkflowBase> getWorkflowById(@PathVariable UUID id) {
        return workflowService.getWorkflowById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> updateWorkflow(
            @PathVariable UUID id,
            @RequestHeader("If-Match") Long version,
            @Valid @RequestBody WorkflowRequest request) {
        return workflowService.updateWorkflow(id, request, version);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteWorkflow(@PathVariable UUID id) {
        return workflowService.deleteWorkflow(id);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateWorkflowStatus(
            @PathVariable UUID id,
            @RequestParam WorkflowStatus status) {
        return workflowService.updateWorkflowStatus(id, status);
    }

    @GetMapping("/{id}/stages")
    public Flux<Stage> getStagesForWorkflow(@PathVariable UUID id) {
        return workflowService.getStagesForWorkflow(id);
    }

    @PostMapping("/{id}/stages")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UUID> addStageToWorkflow(
            @PathVariable UUID id,
            @Valid @RequestBody Stage stage) {
        return workflowService.addStageToWorkflow(id, stage);
    }

    @GetMapping("/{id}/stages/{stageId}")
    public Mono<Stage> getStageById(@PathVariable UUID id, @PathVariable UUID stageId) {
        return workflowService.getStageById(stageId);
    }

    @PatchMapping("/{id}/stages/{stageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateStage(
            @PathVariable UUID id,
            @PathVariable UUID stageId,
            @Valid @RequestBody StageRequest request) {
        return workflowService.updateStage(stageId, request);
    }

    @DeleteMapping("/{id}/stages/{stageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteStage(@PathVariable UUID id, @PathVariable UUID stageId) {
        return workflowService.deleteStage(stageId);
    }

}
