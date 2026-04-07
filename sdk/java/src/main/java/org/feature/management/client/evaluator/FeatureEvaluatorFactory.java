package org.feature.management.client.evaluator;

import org.feature.management.models.FeatureConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for retrieving the appropriate evaluator for a given feature strategy.
 */
public class FeatureEvaluatorFactory {
    private final Map<String, FeatureEvaluator<? extends FeatureConfiguration>> evaluators = new HashMap<>();

    public FeatureEvaluatorFactory(List<FeatureEvaluator<? extends FeatureConfiguration>> evaluatorList) {
        if (evaluatorList != null) {
            for (FeatureEvaluator<? extends FeatureConfiguration> evaluator : evaluatorList) {
                evaluators.put(evaluator.getStrategyName(), evaluator);
            }
        }
    }

    /**
     * Gets an evaluator for the given strategy name.
     * @param strategyName The name of the strategy (from OpenAPI spec)
     * @return The evaluator, or null if not found.
     */
    public FeatureEvaluator<? extends FeatureConfiguration> getEvaluator(String strategyName) {
        return evaluators.get(strategyName);
    }
}
