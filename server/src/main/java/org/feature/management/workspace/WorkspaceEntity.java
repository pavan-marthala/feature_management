package org.feature.management.workspace;

import lombok.*;

import java.util.UUID;

import org.feature.management.shared.utils.ETaggableEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("workspace")
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceEntity implements ETaggableEntity {

    @Id
    private UUID id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("modified_at")
    private Instant modifiedAt;

    @Version
    @Column("version")
    private Long version;

    @Override
    public Long getEtag() {
        return version;
    }
}
