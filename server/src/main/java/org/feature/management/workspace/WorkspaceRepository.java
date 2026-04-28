package org.feature.management.workspace;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface WorkspaceRepository extends R2dbcRepository<WorkspaceEntity, UUID> {
    Flux<WorkspaceEntity> findBy(Pageable pageable);

    Mono<Boolean> existsByName(String name);
}
