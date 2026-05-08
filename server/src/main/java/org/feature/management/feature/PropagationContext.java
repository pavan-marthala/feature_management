package org.feature.management.feature;

import java.util.UUID;

public record PropagationContext(FeatureEntity source, UUID targetEnvId, FeatureEntity savedFeature) {
    PropagationContext(FeatureEntity source, UUID targetEnvId) {
        this(source, targetEnvId, null);
    }

    PropagationContext withSavedFeature(FeatureEntity saved) {
        return new PropagationContext(source, targetEnvId, saved);
    }
}
