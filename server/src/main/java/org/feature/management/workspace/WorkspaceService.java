package org.feature.management.workspace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.feature.management.feature.FeatureEntity;
import org.feature.management.feature.FeatureMapper;
import org.feature.management.feature.FeatureRepository;
import org.feature.management.models.*;
import org.feature.management.shared.exception.AccessDeniedException;
import org.feature.management.shared.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkspaceService implements WorkspaceServiceInterface {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;

    @Override
    public Mono<UUID> createWorkspace(WorkspaceRequest request) {
        log.debug("Creating workspace with name: {}", request.getName());
        return workspaceRepository.existsByName(request.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono
                                .error(new DataIntegrityViolationException("Workspace with same name already exists"));
                    }
                    WorkspaceEntity entity = workspaceMapper.toEntity(request);
                    entity.setId(UUID.randomUUID());
                    return workspaceRepository.save(entity)
                            .map(WorkspaceEntity::getId);
                });
    }

    @Override
    public Mono<WorkspaceResponse> getWorkspaces(Integer page, Integer size) {
        log.debug("Getting all workspaces page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size);
        return workspaceRepository.findBy(pageRequest)
                .map(workspaceMapper::toModel)
                .collectList()
                .zipWith(workspaceRepository.count())
                .map(tuple -> WorkspaceResponse.builder()
                        .items(tuple.getT1())
                        .page(page)
                        .size(size)
                        .totalItems(tuple.getT2().intValue())
                        .totalPages((int) Math.ceil((double) tuple.getT2() / size))
                        .build());
    }

    @Override
    public Mono<Workspace> getWorkspaceById(UUID id) {
        log.debug("Getting workspace by id: {}", id);
        return getWorkspaceEntity(id)
                .map(workspaceMapper::toModel);
    }

    private <T> void updateIfNotNull(Consumer<T> setter, T value) {
        Optional.ofNullable(value).ifPresent(setter);
    }

    @Override
    public Mono<Void> updateWorkspace(UUID id, WorkspaceRequest request) {
        log.debug("Updating workspace id: {}", id);
        return getWorkspaceEntity(id)
                .map(workspace -> {
                    updateIfNotNull(workspace::setName, request.getName());
                    updateIfNotNull(workspace::setDescription, request.getDescription());
                    return workspace;
                })
                .flatMap(workspaceRepository::save)
                .then();
    }

    @Override
    public Mono<Void> deleteWorkspace(UUID id) {
        log.debug("Deleting workspace id: {}", id);
        return getWorkspaceEntity(id)
                .flatMap(workspace -> featureRepository.countByWorkspaceId(id)
                        .flatMap(count -> {
                            if (count > 0) {
                                return Mono.error(new AccessDeniedException(
                                        "Cannot delete workspace with features"));
                            }
                            return workspaceRepository.delete(workspace);
                        }));
    }

    @Override
    public Mono<GetWorkspaceSummary200Response> getWorkspaceSummary(UUID id) {
        log.debug("Getting summary for workspace id: {}", id);
        return getWorkspaceEntity(id)
                .flatMap(workspace -> Mono.zip(
                        featureRepository.countByWorkspaceId(id),
                        Mono.just(0L)).map(tuple -> {
                    GetWorkspaceSummary200Response summary = new GetWorkspaceSummary200Response();
                    summary.setFeatureCount(tuple.getT1().intValue());
                    summary.setWorkflowStages(tuple.getT2().intValue());
                    summary.setEnvironments(0);
                    return summary;
                }));
    }

    @Override
    public Mono<FeatureResponse> getWorkspaceFeatures(UUID id, UUID environmentId, Integer page, Integer size) {
        log.debug("Getting features for workspace id: {} and environment id: {}", id, environmentId);
        PageRequest pageRequest = PageRequest.of(page, size, org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        Flux<FeatureEntity> featuresFlux;
        Mono<Long> countMono;

        if (environmentId != null) {
            featuresFlux = featureRepository.findByWorkspaceIdAndEnvironmentId(id, environmentId, pageRequest);
            countMono = featureRepository.countByWorkspaceIdAndEnvironmentId(id, environmentId);
        } else {
            featuresFlux = featureRepository.findByWorkspaceId(id, pageRequest);
            countMono = featureRepository.countByWorkspaceId(id);
        }

        return featuresFlux
                .map(featureMapper::toModel)
                .collectList()
                .zipWith(countMono)
                .map(tuple -> FeatureResponse.builder()
                        .items(tuple.getT1())
                        .page(page)
                        .size(size)
                        .totalItems(tuple.getT2().intValue())
                        .totalPages((int) Math.ceil((double) tuple.getT2() / size))
                        .build());
    }

    private Mono<WorkspaceEntity> getWorkspaceEntity(UUID id) {
        return workspaceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Workspace not found")));
    }
}
