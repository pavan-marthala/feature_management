package org.feature.management.client.evaluator;

import org.feature.management.client.context.EvaluationContext;
import org.feature.management.models.JWTClaimFeatureStrategy;

public class JWTClaimEvaluator implements FeatureEvaluator<JWTClaimFeatureStrategy> {

    @Override
    public boolean evaluate(JWTClaimFeatureStrategy strategy, EvaluationContext context) {
        // Implementation for JWT claims will be added here
        return false;
    }

    @Override
    public String getStrategyName() {
        return "JWTClaimFeatureStrategy";
    }
}
