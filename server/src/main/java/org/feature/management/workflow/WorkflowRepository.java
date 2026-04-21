package org.feature.management.workflow;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface WorkflowRepository extends ReactiveCrudRepository<WorkflowEntity, UUID> {
    Flux<WorkflowEntity> findBy(PageRequest pageRequest);
}
