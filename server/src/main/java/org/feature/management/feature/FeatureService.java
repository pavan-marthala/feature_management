package org.feature.management.feature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.feature.management.config.FeatureStrategyConfig;
import org.feature.management.models.*;
import org.feature.management.propagation.PropagationHistoryEntity;
import org.feature.management.propagation.PropagationHistoryRepository;
import org.feature.management.shared.exception.AccessDeniedException;
import org.feature.management.shared.exception.EnvironmentException;
import org.feature.management.shared.exception.FeatureException;
import org.feature.management.shared.exception.ResourceNotFoundException;
import org.feature.management.shared.utils.SortHelper;
import org.feature.management.workflow.StageEntity;
import org.feature.management.workflow.StageRepository;
import org.feature.management.workflow.WorkflowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeatureService implements FeatureServiceInterface {

    private final FeatureRepository featureRepo;
    private final FeatureStrategyConfig featureStrategyConfig;
    private final WorkflowRepository workflowRepository;
    private final StageRepository stageRepository;
    private final PropagationHistoryRepository propagationHistoryRepo;
    private final FeatureMapper featureMapper;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Void> assignOwnerToFeature(UUID featureId, String owner) {
        log.debug("Assigning owner {} to feature {}", owner, featureId);
        return getFeatureEntity(featureId)
                .map(feature -> {
                    feature.getOwners().add(owner);
                    return feature;
                })
                .flatMap(featureRepo::save)
                .then();
    }

    @Override
    public Mono<Void> removeOwnerFromFeature(UUID featureId, String owner) {
        log.debug("Removing owner {} from feature {}", owner, featureId);
        return getFeatureEntity(featureId)
                .flatMap(feature -> validateOwnerRemoval(feature, owner))
                .flatMap(feature -> {
                    feature.getOwners().remove(owner);
                    return featureRepo.save(feature);
                })
                .then();
    }

    private Mono<FeatureEntity> validateOwnerRemoval(FeatureEntity feature, String owner) {
        return Mono.just(feature)
                .filter(f -> f.getOwners() != null && f.getOwners().contains(owner))
                .switchIfEmpty(Mono.error(new AccessDeniedException(
                        "Access denied. Only owner of the feature can remove the owner.")))
                .filter(f -> f.getOwners().size() > 1)
                .switchIfEmpty(Mono.error(new EnvironmentException(
                        "Cannot remove the last owner from feature. At least one owner is required.")));
    }

    @Override
    public Mono<Page<Feature>> getAllFeatures(Integer page, Integer size, String sort) {
        log.debug("Fetching features with pagination, page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, SortHelper.buildSort(sort));
        return featureRepo.findBy(pageRequest)
                .map(featureMapper::toModel)
                .collectList()
                .zipWith(featureRepo.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }


    @Override
    @Transactional
    public Mono<UUID> createFeature(FeatureCreateRequest featureRequest) {
        log.debug("Creating feature with request: {}", featureRequest);
        return workflowRepository.findById(featureRequest.getWorkflowId())
                .switchIfEmpty(Mono.error(new FeatureException("Workflow not found for feature")))
                .flatMap(workflow -> stageRepository.findFirstByWorkflowIdOrderByOrderIndexAsc(workflow.getId())
                        .switchIfEmpty(Mono.error(new FeatureException("Workflow must contain at least one stage before creating features"))))
                .flatMap(firstStage -> assertFeatureNameUnique(featureRequest.getName(), firstStage.getEnvironmentId(), featureRequest.getWorkspaceId())
                        .then(Mono.fromSupplier(() -> {
                            FeatureEntity entity = featureMapper.toEntity(featureRequest);
                            entity.setEnvironmentId(firstStage.getEnvironmentId());
                            return entity;
                        })))
                .flatMap(featureRepo::save)
                .map(FeatureEntity::getId);
    }

    private Mono<Void> assertFeatureNameUnique(String name, UUID environmentId, UUID workspaceId) {
        return featureRepo.existsByNameAndEnvironmentIdAndWorkspaceId(name, environmentId, workspaceId)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new FeatureException(
                        "Feature with name " + name + " already exists in this environment")))
                .then();
    }

    @Override
    public Mono<Feature> getById(String id, IdType idType, UUID environmentId) {
        log.debug("Fetching feature by id: {} with type: {}", id, idType);
        return switch (idType) {
            case ID -> getFeatureEntity(UUID.fromString(id)).map(featureMapper::toModel);
            case NAME -> {
                if (environmentId == null) {
                    yield Mono.error(new IllegalArgumentException(
                            "environmentId is required when fetching by NAME"));
                }
                yield featureRepo.getByNameAndEnvironmentId(id, environmentId)
                        .map(featureMapper::toModel)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                "Feature not found with name: " + id + " in environment: " + environmentId)));
            }
        };
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        log.debug("Deleting feature by id: {}", id);
        return getFeatureEntity(id)
                .flatMap(featureRepo::delete)
                .then();
    }

    @Override
    public Flux<FeatureStrategyResponseInner> getAllFeatureStrategies() {
        log.debug("Fetching all feature strategies");
        return Flux.fromIterable(featureStrategyConfig.getStrategies());
    }

    @Override
    public Mono<Feature> getFeatureByNameAndEnvironmentId(String name, UUID environmentId) {
        log.debug("Fetching feature by name: {} in environment: {}", name, environmentId);
        return featureRepo.getByNameAndEnvironmentId(name, environmentId)
                .map(featureMapper::toModel);
    }

    @Override
    public Mono<Void> updateFeature(UUID id, FeatureConfiguration configuration) {
        log.debug("Updating feature configuration with id: {}", id);
        return getFeatureEntity(id)
                .map(feature -> {
                    feature.setConfiguration(configuration);
                    return feature;
                })
                .flatMap(featureRepo::save)
                .then();
    }

    @Override
    public Mono<Void> updateFeatureStatus(UUID id, boolean enabled) {
        log.debug("Updating feature status with id: {} to enabled: {}", id, enabled);
        return getFeatureEntity(id)
                .map(feature -> {
                    feature.setEnabled(enabled);
                    return feature;
                })
                .flatMap(featureRepo::save)
                .then();
    }

    private Mono<FeatureEntity> getFeatureEntity(UUID featureId) {
        return featureRepo.findById(featureId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Feature not found with id: " + featureId)));
    }

    @Override
    public Mono<FeaturePromotionResponse> propagateFeature(UUID featureId) {
        log.info("Propagating feature {}", featureId);
        return transactionalOperator.transactional(
                getFeatureEntity(featureId)
                        .flatMap(this::resolveTargetEnvironment)
                        .flatMap(this::upsertFeatureInTargetEnvironment)
                        .flatMap(this::recordAndBuildResponse)
        );
    }

    private Mono<PropagationContext> resolveTargetEnvironment(FeatureEntity source) {
        return workflowRepository.findById(source.getWorkflowId())
                .switchIfEmpty(Mono.error(new FeatureException("Workflow not found for feature")))
                .flatMap(_ -> findNextStageEnvironment(source.getWorkflowId(), source.getEnvironmentId()))
                .map(targetEnvId -> new PropagationContext(source, targetEnvId));
    }

    private Mono<PropagationContext> upsertFeatureInTargetEnvironment(PropagationContext ctx) {
        return featureRepo.getByNameAndWorkspaceIdAndEnvironmentId(
                        ctx.source().getName(),
                        ctx.source().getWorkspaceId(),
                        ctx.targetEnvId())
                .map(existing -> {
                    existing.setConfiguration(ctx.source().getConfiguration());
                    existing.setEnabled(ctx.source().isEnabled());
                    return existing;
                })
                .switchIfEmpty(Mono.fromSupplier(() -> buildNewFeature(ctx.source(), ctx.targetEnvId())))
                .flatMap(featureRepo::save)
                .map(ctx::withSavedFeature);
    }

    private Mono<FeaturePromotionResponse> recordAndBuildResponse(PropagationContext ctx) {
        return recordPropagationHistory(
                ctx.source().getId(),
                ctx.savedFeature().getId(),
                ctx.source().getEnvironmentId(),
                ctx.targetEnvId(),
                PromotionStatus.SUCCESS)
                .thenReturn(FeaturePromotionResponse.builder()
                        .id(ctx.savedFeature().getId())
                        .status(PromotionStatus.SUCCESS)
                        .build());
    }

    private FeatureEntity buildNewFeature(FeatureEntity source, UUID targetEnvId) {
        return FeatureEntity.builder()
                .id(UUID.randomUUID())
                .name(source.getName())
                .description(source.getDescription())
                .workspaceId(source.getWorkspaceId())
                .workflowId(source.getWorkflowId())
                .environmentId(targetEnvId)
                .configuration(source.getConfiguration())
                .enabled(source.isEnabled())
                .owners(source.getOwners())
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .build();
    }

    @Override
    public Flux<PropagationHistory> getPropagationHistory(UUID id) {
        return propagationHistoryRepo.findAllBySourceFeatureIdOrderByCreatedAtDesc(id)
                .map(entity -> PropagationHistory.builder()
                        .id(entity.getId())
                        .sourceFeatureId(entity.getSourceFeatureId())
                        .targetFeatureId(entity.getTargetFeatureId())
                        .sourceEnvironmentId(entity.getSourceEnvironmentId())
                        .targetEnvironmentId(entity.getTargetEnvironmentId())
                        .promotedBy(entity.getPromotedBy())
                        .status(entity.getStatus())
                        .build());
    }

    private Mono<UUID> findNextStageEnvironment(UUID workflowId, UUID currentEnvId) {
        return stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)
                .collectList().flatMap(stages -> nextEnvironmentId(stages, currentEnvId));
    }

    private Mono<UUID> nextEnvironmentId(List<?> stages, UUID currentEnvId) {
        return Flux.fromIterable(stages)
                .index()
                .filter(indexed -> ((StageEntity) indexed.getT2()).getEnvironmentId().equals(currentEnvId))
                .next()
                .flatMap(indexed -> {
                    int nextIndex = (int) (long) indexed.getT1() + 1;
                    return nextIndex < stages.size()
                            ? Mono.just(((StageEntity) stages.get(nextIndex)).getEnvironmentId())
                            : Mono.error(new FeatureException(
                            "Current environment is not in the specified workflow or is the last stage"));
                })
                .switchIfEmpty(Mono.error(new FeatureException(
                        "Current environment is not in the specified workflow or is the last stage")));
    }

    private Mono<Void> recordPropagationHistory(
            UUID sourceFeatureId,
            UUID targetFeatureId,
            UUID sourceEnvId,
            UUID targetEnvId,
            PromotionStatus status) {
        log.info("Recording propagation history: {} -> {}", sourceFeatureId, targetFeatureId);
        PropagationHistoryEntity history = PropagationHistoryEntity.builder().id(UUID.randomUUID()).sourceFeatureId(sourceFeatureId).targetFeatureId(targetFeatureId).sourceEnvironmentId(sourceEnvId).targetEnvironmentId(targetEnvId).status(status).completedAt(Instant.now()).build();
        return propagationHistoryRepo.save(history)
                .doOnSuccess(saved -> log.info("Saved propagation history: {}", saved.getId()))
                .doOnError(err -> log.error("Failed to save propagation history", err))
                .then();
    }

}
