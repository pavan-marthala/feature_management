package org.feature.management.workflow;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface StageRepository extends ReactiveCrudRepository<StageEntity, UUID> {
    Flux<StageEntity> findAllByWorkflowIdOrderByOrderIndexAsc(UUID workflowId);
    Mono<Void> deleteAllByWorkflowId(UUID workflowId);
}
