package org.feature.management.propagation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.feature.management.models.PromotionStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("propagation_history")
public class PropagationHistoryEntity {

    @Id
    private UUID id;

    @Column("feature_id")
    private UUID featureId;

    @Column("source_environment_id")
    private UUID sourceEnvironmentId;

    @Column("target_environment_id")
    private UUID targetEnvironmentId;

    @Column("promoted_by")
    private String promotedBy;

    @Column("status")
    private PromotionStatus status;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @Column("completed_at")
    private Instant completedAt;
}
