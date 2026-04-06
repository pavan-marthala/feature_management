package org.feature.management.environment;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("feature_environment_mapping")
public class FeatureEnvironmentMappingEntity {
    private UUID featureId;
    private UUID environmentId;

    @CreatedDate
    private LocalDateTime createdAt;
}
