package org.feature.management.workspace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.feature.management.feature.FeatureMapper;
import org.feature.management.feature.FeatureRepository;
import org.feature.management.models.FeatureResponse;
import org.feature.management.models.GetWorkspaceSummary200Response;
import org.feature.management.models.Workspace;
import org.feature.management.models.WorkspaceRequest;
import org.feature.management.models.WorkspaceResponse;
import org.feature.management.shared.exception.AccessDeniedException;
import org.feature.management.shared.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;

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

    public Mono<Workspace> getWorkspaceById(UUID id) {
        log.debug("Getting workspace by id: {}", id);
        return getWorkspaceEntity(id)
                .map(workspaceMapper::toModel);
    }

    private <T> void updateIfNotNull(Consumer<T> setter, T value) {
        Optional.ofNullable(value).ifPresent(setter);
    }

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

    public Mono<FeatureResponse> getWorkspaceFeatures(UUID id, Integer page, Integer size) {
        log.debug("Getting features for workspace id: {}", id);
        PageRequest pageRequest = PageRequest.of(page, size);
        return featureRepository.findByWorkspaceId(id, pageRequest)
                .map(featureMapper::toModel)
                .collectList()
                .zipWith(featureRepository.countByWorkspaceId(id))
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
