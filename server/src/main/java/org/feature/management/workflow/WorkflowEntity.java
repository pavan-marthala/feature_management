package org.feature.management.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.feature.management.models.WorkflowStatus;
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
@Table("workflow")
public class WorkflowEntity implements ETaggableEntity {

    @Id
    private UUID id;

    @Column("name")
    private String name;

    @Column("status")
    private WorkflowStatus status;

    @Version
    @Column("version")
    private Long version;

    @Column("created_at")
    @CreatedDate
    private Instant createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public Long getEtag() {
        return version;
    }
}
