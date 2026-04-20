package org.feature.management.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.feature.management.models.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final StageRepository stageRepository;
    private final WorkflowMapper workflowMapper;

    public Mono<WorkflowResponse> getAllWorkflows(int page, int size) {
        return workflowRepository.findAll()
                .skip((long) (page - 1) * size)
                .take(size)
                .map(workflowMapper::toBaseModel)
                .collectList()
                .zipWith(workflowRepository.count())
                .map(tuple -> {
                    WorkflowResponse response = new WorkflowResponse();
                    response.setItems(tuple.getT1());
                    response.setTotalItems(tuple.getT2().intValue());
                    response.setPage(page);
                    response.setSize(size);
                    response.setTotalPages((int) Math.ceil((double) tuple.getT2() / size));
                    return response;
                });
    }

    public Mono<UUID> createWorkflow(WorkflowBase workflowBase) {
        WorkflowEntity entity = workflowMapper.toEntity(workflowBase);
        entity.setId(UUID.randomUUID());
        entity.setStatus(WorkflowStatus.DRAFT);
        return workflowRepository.save(entity).map(WorkflowEntity::getId);
    }

    public Mono<WorkflowBase> getWorkflowById(UUID id) {
        return workflowRepository.findById(id)
                .map(workflowMapper::toBaseModel);
    }

    public Mono<Void> updateWorkflow(UUID id, WorkflowRequest request, Long version) {
        return workflowRepository.findById(id)
                .flatMap(entity -> {
                    entity.setName(request.getName());
                    return workflowRepository.save(entity);
                })
                .then();
    }

    public Mono<Void> updateWorkflowStatus(UUID id, WorkflowStatus status) {
        return workflowRepository.findById(id)
                .flatMap(entity -> {
                    entity.setStatus(status);
                    return workflowRepository.save(entity);
                })
                .then();
    }

    public Mono<Void> deleteWorkflow(UUID id) {
        return workflowRepository.deleteById(id);
    }

    public Flux<Stage> getStagesForWorkflow(UUID workflowId) {
        return stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)
                .map(workflowMapper::toModel);
    }

    public Mono<UUID> addStageToWorkflow(UUID workflowId, Stage stage) {
        StageEntity entity = workflowMapper.toEntity(stage);
        entity.setId(UUID.randomUUID());
        entity.setWorkflowId(workflowId);
        return stageRepository.save(entity).map(StageEntity::getId);
    }

    public Mono<Stage> getStageById(UUID stageId) {
        return stageRepository.findById(stageId)
                .map(workflowMapper::toModel);
    }

    public Mono<Void> updateStage(UUID stageId, StageRequest request) {
        return stageRepository.findById(stageId)
                .flatMap(entity -> {
                    entity.setEnvironmentId(request.getEnvironmentId());
                    entity.setOrderIndex(request.getOrderIndex());
                    entity.setType(request.getType());
                    entity.setScheduleExpression(request.getScheduleExpression());
                    return stageRepository.save(entity);
                })
                .then();
    }

    public Mono<Void> deleteStage(UUID stageId) {
        return stageRepository.deleteById(stageId);
    }
}
