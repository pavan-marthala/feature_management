package org.feature.management.client.context;

import reactor.core.publisher.Mono;

import java.util.Collections;

public class DefaultEvaluationContextExtractor implements EvaluationContextExtractor {

    @Override
    public Mono<EvaluationContext> extractContext() {
        return Mono.just(EvaluationContext.builder()
                .attributes(Collections.emptyMap())
                .headers(Collections.emptyMap())
                .roles(Collections.emptyList())
                .build());
    }
}
