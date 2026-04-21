package org.feature.management.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.feature.management.models.StageType;
import org.feature.management.shared.utils.ETaggableEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("stage")
public class StageEntity implements ETaggableEntity {

    @Id
    private UUID id;

    @Column("workflow_id")
    private UUID workflowId;

    @Column("environment_id")
    private UUID environmentId;

    @Column("environment_name")
    private String environmentName;

    @Column("order_index")
    private Integer orderIndex;

    @Column("type")
    private StageType type;

    @Column("next_stage_id")
    private UUID nextStageId;

    @Column("schedule_expression")
    private String scheduleExpression;

    @Column("approval_needed")
    private Boolean approvalNeeded;

    @Column("created_at")
    @CreatedDate
    private Instant createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    @Version
    @Column("version")
    private Long version;

    @Override
    public Long getEtag() {
        return version;
    }
}
