package org.feature.management.client.evaluator;

import org.feature.management.client.context.EvaluationContext;
import org.feature.management.models.BooleanFeatureStrategy;

public class BooleanFeatureEvaluator implements FeatureEvaluator<BooleanFeatureStrategy> {

    @Override
    public boolean evaluate(BooleanFeatureStrategy strategy, EvaluationContext context) {
        return Boolean.TRUE.equals(strategy.getValue());
    }

    @Override
    public String getStrategyName() {
        return "BooleanFeatureStrategy";
    }
}
