package org.feature.management.client.context;

import reactor.core.publisher.Mono;

public interface EvaluationContextExtractor {
    Mono<EvaluationContext> extractContext();
}
