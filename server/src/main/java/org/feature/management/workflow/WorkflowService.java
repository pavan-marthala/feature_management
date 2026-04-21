package org.feature.management.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.feature.management.environment.EnvironmentEntity;
import org.feature.management.environment.EnvironmentRepository;
import org.feature.management.models.*;
import org.feature.management.shared.exception.ResourceNotFoundException;
import org.feature.management.shared.utils.SortHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final StageRepository stageRepository;
    private final WorkflowMapper workflowMapper;
    private final EnvironmentRepository environmentRepository;


    public Mono<Page<WorkflowBase>> getAllWorkflows(Integer page, Integer size, String sort) {
        log.debug("Fetching workflows with pagination, page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, SortHelper.buildSort(sort));
        return workflowRepository.findBy(pageRequest)
                .map(workflowMapper::toBaseModel)
                .collectList()
                .zipWith(workflowRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    public Mono<UUID> createWorkflow(WorkflowBase workflowBase) {
        WorkflowEntity entity = workflowMapper.toEntity(workflowBase);
        entity.setId(UUID.randomUUID());
        return workflowRepository.save(entity).map(WorkflowEntity::getId);
    }

    public Mono<WorkflowBase> getWorkflowById(UUID id) {
        return getWorkflow(id).map(workflowMapper::toBaseModel);
    }

    private @NonNull Mono<WorkflowEntity> getWorkflow(UUID id) {
        return workflowRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Workflow not found with id: " + id)));
    }

    public Mono<Void> updateWorkflow(UUID id, WorkflowBase request, Long version) {
        return getWorkflow(id)
                .flatMap(entity -> {
                    entity.setName(request.getName());
                    entity.setStatus(request.getStatus());
                    return workflowRepository.save(entity);
                })
                .then();
    }

    public Mono<Void> updateWorkflowStatus(UUID id, WorkflowStatus status) {
        return getWorkflow(id)
                .flatMap(entity -> {
                    entity.setStatus(status);
                    return workflowRepository.save(entity);
                })
                .then();
    }

    public Mono<Void> deleteWorkflow(UUID id) {
        return getWorkflow(id)
                .flatMap(workflow -> stageRepository.deleteAllByWorkflowId(workflow.getId()).then(workflowRepository.delete(workflow)))
                .then();
    }

    public Mono<Workflow> getStagesForWorkflow(UUID workflowId) {
        return getWorkflow(workflowId).flatMap(workflowEntity -> stageRepository
                .findAllByWorkflowIdOrderByOrderIndexAsc(workflowId).map(workflowMapper::toModel).collectList()
                .map(stages -> Workflow.builder().id(workflowEntity.getId()).name(workflowEntity.getName()).status(workflowEntity.getStatus()).version(workflowEntity.getVersion()).stages(stages).build()));
    }

    public Mono<UUID> addStageToWorkflow(UUID workflowId, Stage stage) {
        StageEntity entity = workflowMapper.toEntity(stage);
        entity.setId(UUID.randomUUID());
        entity.setWorkflowId(workflowId);
        Integer orderIndex = stage.getOrderIndex() != null ? stage.getOrderIndex() : 0;

        if (orderIndex == 0) {
           return getEnvironment(entity)
                   .flatMap(environmentEntity -> {
                       entity.setEnvironmentName(environmentEntity.getName());
                       entity.setEnvironmentId(environmentEntity.getId());
                       return stageRepository.save(entity);
                   }).map(StageEntity::getId);
        }

        return stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)
                .collectList()
                .flatMap(existingStages -> {
                    if (existingStages.isEmpty()) {
                        return getEnvironment(entity)
                                .flatMap(environmentEntity -> {
                                    entity.setEnvironmentName(environmentEntity.getName());
                                    entity.setEnvironmentId(environmentEntity.getId());
                                    return stageRepository.save(entity);
                                }).map(StageEntity::getId);
                    }

                    UUID newStageId = entity.getId();

                    // Determine what the new stage should point to
                    UUID nextStageIdForNewStage = (orderIndex < existingStages.size())
                            ? existingStages.get(orderIndex).getId()
                            : null;

                    if (stage.getNextStageId() != null) {
                        entity.setNextStageId(stage.getNextStageId());
                    } else if (nextStageIdForNewStage != null) {
                        entity.setNextStageId(nextStageIdForNewStage);
                    }

                    // Shift order_index for all stages at or after insertion point
                    List<StageEntity> stagesToShift = existingStages.stream()
                            .filter(s -> s.getOrderIndex() >= orderIndex)
                            .peek(s -> s.setOrderIndex(s.getOrderIndex() + 1))
                            .toList();

                    // Previous stage needs to point to the new stage
                    StageEntity previousStage = (orderIndex - 1 < existingStages.size())
                            ? existingStages.get(orderIndex - 1)
                            : null;

                    // Step 1: Shift existing stages FIRST (avoids order_index conflicts)
                    // Step 2: Save the new stage (FK target now valid for previousStage update)
                    // Step 3: Update previousStage.next_stage_id to point to new stage
                    return Flux.fromIterable(stagesToShift)
                            .concatMap(stageRepository::save)   // shift in order
                            .then(enrichWithEnvironmentName(entity))
                            .flatMap(stageRepository::save)
                            .flatMap(saved -> {
                                if (previousStage != null) {
                                    previousStage.setNextStageId(newStageId);
                                    return stageRepository.save(previousStage).thenReturn(saved);
                                }
                                return Mono.just(saved);
                            })
                            .map(StageEntity::getId);
                });
    }
    public Mono<Stage> getStageById(UUID stageId) {
        return getStage(stageId).map(workflowMapper::toModel);
    }

    private @NonNull Mono<StageEntity> getStage(UUID stageId) {
        return stageRepository.findById(stageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Stage not found with id: " + stageId)));
    }

    public Mono<Void> updateStage(UUID stageId, StageRequest request) {
        return getStage(stageId)
                .flatMap(entity -> {
                    entity.setOrderIndex(request.getOrderIndex());
                    entity.setType(request.getType());
                    entity.setScheduleExpression(request.getScheduleExpression());

                    boolean envChanged = request.getEnvironmentId() != null
                            && !request.getEnvironmentId().equals(entity.getEnvironmentId());

                    if (envChanged) {
                        entity.setEnvironmentId(request.getEnvironmentId());
                        return enrichWithEnvironmentName(entity);
                    }
                    return Mono.just(entity);
                })
                .flatMap(stageRepository::save)
                .then();
    }

    public Mono<Void> deleteStage(UUID stageId) {
        return getStage(stageId).flatMap(stageRepository::delete).then();
    }

    private Mono<StageEntity> enrichWithEnvironmentName(StageEntity entity) {
        if (entity.getEnvironmentId() == null) return Mono.just(entity);
        return getEnvironment(entity)
                .doOnNext(env -> entity.setEnvironmentName(env.getName()))
                .thenReturn(entity);
    }

    private @NonNull Mono<EnvironmentEntity> getEnvironment(StageEntity entity) {
        return environmentRepository.findById(entity.getEnvironmentId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Environment not found with id: " + entity.getEnvironmentId())));
    }
}
