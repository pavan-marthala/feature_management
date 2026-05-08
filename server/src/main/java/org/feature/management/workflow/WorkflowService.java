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
public class WorkflowService implements WorkflowServiceInterface {

    private final WorkflowRepository workflowRepository;
    private final StageRepository stageRepository;
    private final WorkflowMapper workflowMapper;
    private final EnvironmentRepository environmentRepository;


    @Override
    public Mono<Page<WorkflowBase>> getAllWorkflows(Integer page, Integer size, String sort) {
        log.debug("Fetching workflows with pagination, page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, SortHelper.buildSort(sort));
        return workflowRepository.findBy(pageRequest)
                .map(workflowMapper::toBaseModel)
                .collectList()
                .zipWith(workflowRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    @Override
    public Mono<UUID> createWorkflow(WorkflowBase workflowBase) {
        WorkflowEntity entity = workflowMapper.toEntity(workflowBase);
        entity.setId(UUID.randomUUID());
        return workflowRepository.save(entity).map(WorkflowEntity::getId);
    }

    @Override
    public Mono<WorkflowBase> getWorkflowById(UUID id) {
        return getWorkflow(id).map(workflowMapper::toBaseModel);
    }

    private @NonNull Mono<WorkflowEntity> getWorkflow(UUID id) {
        return workflowRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Workflow not found with id: " + id)));
    }

    @Override
    public Mono<Void> updateWorkflow(UUID id, WorkflowBase request) {
        return getWorkflow(id)
                .flatMap(entity -> {
                    entity.setName(request.getName());
                    entity.setStatus(request.getStatus());
                    return workflowRepository.save(entity);
                })
                .then();
    }

    @Override
    public Mono<Void> updateWorkflowStatus(UUID id, WorkflowStatus status) {
        return getWorkflow(id)
                .flatMap(entity -> {
                    entity.setStatus(status);
                    return workflowRepository.save(entity);
                })
                .then();
    }

    @Override
    public Mono<Void> deleteWorkflow(UUID id) {
        return getWorkflow(id)
                .flatMap(workflow -> stageRepository.deleteAllByWorkflowId(workflow.getId()).then(workflowRepository.delete(workflow)))
                .then();
    }

    @Override
    public Mono<Workflow> getStagesForWorkflow(UUID workflowId) {
        return getWorkflow(workflowId).flatMap(workflowEntity -> stageRepository
                .findAllByWorkflowIdOrderByOrderIndexAsc(workflowId).map(workflowMapper::toModel).collectList()
                .map(stages -> Workflow.builder().id(workflowEntity.getId()).name(workflowEntity.getName()).status(workflowEntity.getStatus()).version(workflowEntity.getVersion()).stages(stages).build()));
    }

    @Override
    public Mono<UUID> addStageToWorkflow(UUID workflowId, Stage stage) {
        StageEntity entity = buildStageEntity(stage, workflowId);
        int orderIndex = stage.getOrderIndex() != null ? stage.getOrderIndex() : 0;

        return stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)
                .collectList()
                .flatMap(existingStages -> insertStage(entity, stage, orderIndex, existingStages));
    }

    private StageEntity buildStageEntity(Stage stage, UUID workflowId) {
        StageEntity entity = workflowMapper.toEntity(stage);
        entity.setId(UUID.randomUUID());
        entity.setWorkflowId(workflowId);
        return entity;
    }


    private Mono<UUID> insertStage(StageEntity entity, Stage stage, int orderIndex,
                                   List<StageEntity> existingStages) {
        return enrichWithEnvironmentName(entity)
                .flatMap(enriched -> existingStages.isEmpty() || orderIndex == 0
                        ? saveDirectly(enriched)
                        : insertAtPosition(enriched, stage, orderIndex, existingStages));
    }

    private Mono<UUID> saveDirectly(StageEntity entity) {
        return stageRepository.save(entity).map(StageEntity::getId);
    }

    private Mono<UUID> insertAtPosition(StageEntity entity, Stage stage, int orderIndex,
                                        List<StageEntity> existingStages) {
        UUID newStageId = entity.getId();

        entity.setNextStageId(resolveNextStageId(stage, orderIndex, existingStages));

        List<StageEntity> stagesToShift = existingStages.stream()
                .filter(s -> s.getOrderIndex() >= orderIndex)
                .peek(s -> s.setOrderIndex(s.getOrderIndex() + 1))
                .toList();

        StageEntity previousStage = orderIndex - 1 < existingStages.size()
                ? existingStages.get(orderIndex - 1)
                : null;

        // Order matters: shift first → save new → update previous pointer
        return Flux.fromIterable(stagesToShift)
                .concatMap(stageRepository::save)
                .then(stageRepository.save(entity))
                .flatMap(saved -> linkPreviousStage(previousStage, newStageId, saved))
                .map(StageEntity::getId);
    }

    private UUID resolveNextStageId(Stage stage, int orderIndex, List<StageEntity> existingStages) {
        return stage.getNextStageId() != null
                ? stage.getNextStageId()
                : orderIndex < existingStages.size()
                  ? existingStages.get(orderIndex).getId()
                  : null;
    }

    private Mono<StageEntity> linkPreviousStage(StageEntity previousStage, UUID newStageId,
                                                StageEntity saved) {
        return previousStage == null
                ? Mono.just(saved)
                : Mono.fromRunnable(() -> previousStage.setNextStageId(newStageId))
                  .then(stageRepository.save(previousStage))
                  .thenReturn(saved);
    }

    @Override
    public Mono<Stage> getStageById(UUID stageId) {
        return getStage(stageId).map(workflowMapper::toModel);
    }

    private @NonNull Mono<StageEntity> getStage(UUID stageId) {
        return stageRepository.findById(stageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Stage not found with id: " + stageId)));
    }

    @Override
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

    @Override
    public Mono<Void> deleteStage(UUID stageId) {
        return getStage(stageId).flatMap(stageRepository::delete).then();
    }

    private Mono<StageEntity> enrichWithEnvironmentName(StageEntity entity) {
        return entity.getEnvironmentId() == null
                ? Mono.just(entity)
                : getEnvironment(entity.getEnvironmentId())
                  .doOnNext(env -> entity.setEnvironmentName(env.getName()))
                  .thenReturn(entity);
    }

    private @NonNull Mono<EnvironmentEntity> getEnvironment(UUID id) {
        return environmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Environment not found with id: " + id)));
    }


}
