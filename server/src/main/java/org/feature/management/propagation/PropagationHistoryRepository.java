package org.feature.management.propagation;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

@Repository
public interface PropagationHistoryRepository extends ReactiveCrudRepository<PropagationHistoryEntity, UUID> {
    Flux<PropagationHistoryEntity> findAllByFeatureIdOrderByCreatedAtDesc(UUID featureId);
}
