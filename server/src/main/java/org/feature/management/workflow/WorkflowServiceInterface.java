package org.feature.management.workflow;

import org.feature.management.models.*;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WorkflowServiceInterface {
    Mono<Page<WorkflowBase>> getAllWorkflows(Integer page, Integer size, String sort);

    Mono<UUID> createWorkflow(WorkflowBase workflowBase);

    Mono<WorkflowBase> getWorkflowById(UUID id);

    Mono<Void> updateWorkflow(UUID id, WorkflowBase request);

    Mono<Void> updateWorkflowStatus(UUID id, WorkflowStatus status);

    Mono<Void> deleteWorkflow(UUID id);

    Mono<Workflow> getStagesForWorkflow(UUID workflowId);

    Mono<UUID> addStageToWorkflow(UUID workflowId, Stage stage);

    Mono<Stage> getStageById(UUID stageId);

    Mono<Void> updateStage(UUID stageId, StageRequest request);

    Mono<Void> deleteStage(UUID stageId);
}
