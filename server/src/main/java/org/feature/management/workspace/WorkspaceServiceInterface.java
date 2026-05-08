package org.feature.management.workspace;

import org.feature.management.models.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WorkspaceServiceInterface {
    Mono<UUID> createWorkspace(WorkspaceRequest request);

    Mono<WorkspaceResponse> getWorkspaces(Integer page, Integer size);

    Mono<Workspace> getWorkspaceById(UUID id);

    Mono<Void> updateWorkspace(UUID id, WorkspaceRequest request);

    Mono<Void> deleteWorkspace(UUID id);

    Mono<GetWorkspaceSummary200Response> getWorkspaceSummary(UUID id);

    Mono<FeatureResponse> getWorkspaceFeatures(UUID id, UUID environmentId, Integer page, Integer size);


}
