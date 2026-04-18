package org.feature.management.feature;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface FeatureRepository extends R2dbcRepository<FeatureEntity, UUID> {
    Mono<FeatureEntity> getByNameAndEnvironmentId(String name, UUID environmentId);

    Mono<Boolean> existsByNameAndEnvironmentId(String name, UUID environmentId);

    Flux<FeatureEntity> findBy(Pageable pageable);

    Flux<FeatureEntity> findByEnvironmentId(UUID environmentId, Pageable pageable);

    Mono<Long> countByEnvironmentId(UUID environmentId);

    Mono<Long> countByEnabledTrue();

    Mono<Long> countByEnabledFalse();
}
