package org.feature.management.client.evaluator;

import org.feature.management.client.context.EvaluationContext;
import org.feature.management.models.FeatureConfiguration;

/**
 * Interface for evaluating specific feature management strategies.
 * @param <T> The type of strategy this evaluator handles.
 */
public interface FeatureEvaluator<T extends FeatureConfiguration> {
    /**
     * Evaluates the specific strategy against the context.
     */
    boolean evaluate(T strategy, EvaluationContext context);

    /**
     * Returns the name of the strategy this evaluator supports.
     */
    String getStrategyName();
}
