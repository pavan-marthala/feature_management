package org.feature.management.client.evaluator;

import org.feature.management.client.context.EvaluationContext;
import org.feature.management.models.HTTPRequestFeatureStrategy;

public class HTTPRequestEvaluator implements FeatureEvaluator<HTTPRequestFeatureStrategy> {

    @Override
    public boolean evaluate(HTTPRequestFeatureStrategy strategy, EvaluationContext context) {
        // Implementation for HTTP Request based strategy will be added here
        return false;
    }

    @Override
    public String getStrategyName() {
        return "HTTPRequestFeatureStrategy";
    }
}
