package org.feature.management.feature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.feature.management.config.FeatureStrategyConfig;

import org.feature.management.models.Feature;
import org.feature.management.models.FeatureConfiguration;
import org.feature.management.models.FeatureCreateRequest;
import org.feature.management.models.FeatureStrategyResponseInner;
import org.feature.management.models.IdType;
import org.feature.management.models.PromotionStatus;
import org.feature.management.models.PropagationHistory;
import org.feature.management.propagation.*;
import org.feature.management.shared.exception.AccessDeniedException;
import org.feature.management.shared.exception.EnvironmentException;
import org.feature.management.shared.exception.FeatureException;
import org.feature.management.shared.exception.ResourceNotFoundException;
import org.feature.management.shared.utils.SortHelper;
import org.feature.management.workflow.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.feature.management.models.FeaturePromotionRequest;
import org.feature.management.models.FeaturePromotionResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeatureService implements FeatureServiceInterface {

    private final FeatureRepository featureRepo;
    private final FeatureStrategyConfig featureStrategyConfig;
    private final WorkflowRepository workflowRepository;
    private final StageRepository stageRepository;
    private final PropagationHistoryRepository propagationHistoryRepo;

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
                .flatMap(feature -> {
                    Set<String> owners = feature.getOwners();
                    return Mono.just(feature)
                            .filter(_ -> owners != null && owners.contains(owner))
                            .switchIfEmpty(Mono.error(new AccessDeniedException(
                                    "Access denied. Only owner of the feature can remove the owner.")))
                            .filter(_ -> owners.size() > 1)
                            .switchIfEmpty(Mono.error(new EnvironmentException(
                                    "Cannot remove the last owner from feature. At least one owner is required.")))
                            .flatMap(_ -> {
                                owners.remove(owner);
                                return featureRepo.save(feature);
                            });
                })
                .then();
    }

    @Override
    public Mono<Page<Feature>> getAllFeatures(Integer page, Integer size, String sort) {
        log.debug("Fetching features with pagination, page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, SortHelper.buildSort(sort));
        return featureRepo.findBy(pageRequest)
                .map(FeatureMapper.INSTANCE::toModel)
                .collectList()
                .zipWith(featureRepo.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    @Override
    @Transactional
    public Mono<UUID> createFeature(FeatureCreateRequest featureRequest) {
        log.debug("Creating feature with request: {}", featureRequest);
        return featureRepo.existsByNameAndEnvironmentId(featureRequest.getName(), featureRequest.getEnvId())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new FeatureException(
                        "Feature with name " + featureRequest.getName() + " already exists in this environment")))
                .then(Mono.defer(() -> Mono.just(FeatureMapper.INSTANCE.toEntity(featureRequest))))
                .flatMap(featureRepo::save)
                .map(FeatureEntity::getId);
    }

    @Override
    public Mono<Feature> getById(String id, IdType idType, UUID environmentId) {
        log.debug("Fetching feature by id: {} with type: {}", id, idType);
        if (idType == IdType.ID) {
            return getFeatureEntity(UUID.fromString(id))
                    .map(FeatureMapper.INSTANCE::toModel);
        } else {
            if (environmentId == null) {
                return Mono.error(new IllegalArgumentException("environmentId is required when fetching by NAME"));
            }
            return featureRepo.getByNameAndEnvironmentId(id, environmentId)
                    .map(FeatureMapper.INSTANCE::toModel)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                            "Feature not found with name: " + id + " in environment: " + environmentId)));
        }
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
                .map(FeatureMapper.INSTANCE::toModel);
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
    @Transactional
    public Mono<FeaturePromotionResponse> propagateFeature(UUID id, FeaturePromotionRequest request) {
        log.info("Propagating feature {} with request {}", id, request);
        return getFeatureEntity(id)
                .flatMap(sourceFeature -> resolveTargetEnvironment(sourceFeature, request)
                        .flatMap(targetEnvId -> featureRepo
                                .getByNameAndEnvironmentId(sourceFeature.getName(), targetEnvId)
                                .flatMap(existingFeature -> {
                                    existingFeature.setConfiguration(sourceFeature.getConfiguration());
                                    existingFeature.setEnabled(sourceFeature.isEnabled());
                                    return featureRepo.save(existingFeature);
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    FeatureEntity newFeature = FeatureEntity.builder()
                                            .id(UUID.randomUUID())
                                            .name(sourceFeature.getName())
                                            .description(sourceFeature.getDescription())
                                            .environmentId(targetEnvId)
                                            .configuration(sourceFeature.getConfiguration())
                                            .enabled(sourceFeature.isEnabled())
                                            .owners(sourceFeature.getOwners())
                                            .build();
                                    return featureRepo.save(newFeature);
                                }))
                                .flatMap(savedFeature -> recordPropagationHistory(id, sourceFeature.getEnvironmentId(),
                                        targetEnvId, PromotionStatus.SUCCESS)
                                        .thenReturn(FeaturePromotionResponse.builder()
                                                .id(savedFeature.getId())
                                                .status(PromotionStatus.SUCCESS)
                                                .build()))));
    }

    @Override
    public Flux<PropagationHistory> getPropagationHistory(UUID id) {
        return propagationHistoryRepo.findAllByFeatureIdOrderByCreatedAtDesc(id)
                .map(entity -> PropagationHistory.builder()
                        .id(entity.getId())
                        .sourceFeatureId(entity.getFeatureId())
                        .sourceEnvironmentId(entity.getSourceEnvironmentId())
                        .targetEnvironmentId(entity.getTargetEnvironmentId())
                        .promotedBy(entity.getPromotedBy())
                        .status(entity.getStatus())
                        .build());
    }

    private Mono<UUID> resolveTargetEnvironment(FeatureEntity sourceFeature, FeaturePromotionRequest request) {
        if (request.getTargetEnvironmentId() != null) {
            return Mono.just(request.getTargetEnvironmentId());
        }

        UUID workflowId = request.getWorkflowId();

        if (workflowId != null) {
            return findNextStageEnvironment(workflowId, sourceFeature.getEnvironmentId());
        }

        return workflowRepository.findAll()
                .filter(w -> "ACTIVE".equals(w.getStatus()))
                .flatMap(w -> findNextStageEnvironment(w.getId(), sourceFeature.getEnvironmentId()))
                .next()
                .switchIfEmpty(Mono.error(new FeatureException(
                        "No target environment specified and no active workflow found for propagation")));
    }

    private Mono<UUID> findNextStageEnvironment(UUID workflowId, UUID currentEnvId) {
        return stageRepository.findAllByWorkflowIdOrderByOrderIndexAsc(workflowId)
                .collectList()
                .flatMap(stages -> {
                    for (int i = 0; i < stages.size(); i++) {
                        if (stages.get(i).getEnvironmentId().equals(currentEnvId)) {
                            if (i + 1 < stages.size()) {
                                return Mono.just(stages.get(i + 1).getEnvironmentId());
                            }
                        }
                    }
                    return Mono.error(new FeatureException(
                            "Current environment is not in the specified workflow or is the last stage"));
                });
    }

    private Mono<Void> recordPropagationHistory(UUID featureId, UUID sourceEnvId, UUID targetEnvId,
            PromotionStatus status) {
        PropagationHistoryEntity history = PropagationHistoryEntity.builder()
                .id(UUID.randomUUID())
                .featureId(featureId)
                .sourceEnvironmentId(sourceEnvId)
                .targetEnvironmentId(targetEnvId)
                .status(status)
                .completedAt(Instant.now())
                .build();
        return propagationHistoryRepo.save(history).then();
    }

}
