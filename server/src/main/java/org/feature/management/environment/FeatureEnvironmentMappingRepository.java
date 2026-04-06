package org.feature.management.environment;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface FeatureEnvironmentMappingRepository
        extends ReactiveCrudRepository<FeatureEnvironmentMappingEntity, UUID> {

    Flux<FeatureEnvironmentMappingEntity> findByEnvironmentId(UUID environmentId);
}
