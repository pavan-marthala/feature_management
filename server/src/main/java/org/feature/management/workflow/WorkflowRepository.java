package org.feature.management.workflow;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends ReactiveCrudRepository<WorkflowEntity, UUID> {
}
