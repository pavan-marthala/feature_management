package org.feature.management.client.evaluator;

import org.feature.management.client.context.EvaluationContext;
import org.feature.management.models.BooleanFeatureStrategy;
import org.feature.management.models.Feature;

public class FeatureEvaluationManager {

    /**
     * Evaluates the given feature against the provided context.
     * Currently supports BooleanFeatureStrategy as a baseline.
     */
    public boolean evaluate(Feature feature, EvaluationContext context) {
        if (feature == null || Boolean.FALSE.equals(feature.getEnabled())) {
            return false;
        }

        Object config = feature.getConfiguration();
        if (config == null) {
            // Feature is enabled and no specific strategy condition applies
            return true;
        }

        if (config instanceof BooleanFeatureStrategy) {
            Boolean value = ((BooleanFeatureStrategy) config).getValue();
            return value != null && value;
        }

        // For other strategies (JWTClaimFeatureStrategy, ScheduleFeatureStrategy, HTTPRequestFeatureStrategy),
        // evaluation logic can be added here or delegated to specific evaluator components.
        // Failing closed by default for unsupported strategies to prevent unintended access.
        return false;
    }
}
