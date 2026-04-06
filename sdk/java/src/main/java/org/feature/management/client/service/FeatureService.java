package org.feature.management.client.service;

import org.feature.management.client.context.EvaluationContextExtractor;
import org.feature.management.client.evaluator.FeatureEvaluationManager;
import reactor.core.publisher.Mono;

public class FeatureService {

    private final FeatureClient featureFetcher;
    private final FeatureEvaluationManager evaluationManager;
    private final EvaluationContextExtractor contextExtractor;

    public FeatureService(FeatureClient featureFetcher,
                          FeatureEvaluationManager evaluationManager,
                          EvaluationContextExtractor contextExtractor) {
        this.featureFetcher = featureFetcher;
        this.evaluationManager = evaluationManager;
        this.contextExtractor = contextExtractor;
    }

    public Mono<Boolean> isEnabled(String featureName) {
        return Mono.zip(
                featureFetcher.fetchFeature(featureName),
                contextExtractor.extractContext()
        ).map(tuple -> evaluationManager.evaluate(tuple.getT1(), tuple.getT2()))
         // default to false if not found
         .defaultIfEmpty(false)
         // Default fallback is closed (false) if the API fails
         .onErrorReturn(false);
    }
}
