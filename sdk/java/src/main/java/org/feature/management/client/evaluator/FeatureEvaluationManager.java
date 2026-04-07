package org.feature.management.client.evaluator;

import org.feature.management.client.context.EvaluationContext;
import org.feature.management.models.Feature;
import org.feature.management.models.FeatureConfiguration;

public class FeatureEvaluationManager {

    private final FeatureEvaluatorFactory evaluatorFactory;

    public FeatureEvaluationManager(FeatureEvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = evaluatorFactory;
    }

    /**
     * Evaluates the given feature against the provided context.
     * Delegates evaluation to strategy-specific evaluators via the factory.
     */
    @SuppressWarnings("unchecked")
    public boolean evaluate(Feature feature, EvaluationContext context) {
        if (feature == null || Boolean.FALSE.equals(feature.getEnabled())) {
            return false;
        }

        FeatureConfiguration config = feature.getConfiguration();
        if (config == null) {
            // Feature is enabled and no specific strategy condition applies
            return false;
        }

        FeatureEvaluator<FeatureConfiguration> evaluator = (FeatureEvaluator<FeatureConfiguration>) evaluatorFactory
                .getEvaluator(config.getStrategy());

        if (evaluator != null) {
            return evaluator.evaluate(config, context);
        }

        // Failing closed by default for unsupported strategies to prevent unintended
        // access.
        return false;
    }
}
