package org.feature.management.feature;

import org.feature.management.models.Feature;
import org.feature.management.models.FeatureConfiguration;
import org.feature.management.models.FeatureCreateRequest;
import org.feature.management.models.FeaturePromotionRequest;
import org.feature.management.models.FeaturePromotionResponse;
import org.feature.management.models.FeatureStrategyResponseInner;
import org.feature.management.models.IdType;
import org.feature.management.models.PropagationHistory;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FeatureServiceInterface {

    Mono<Void> assignOwnerToFeature(UUID featureId, String owner);

    Mono<Void> removeOwnerFromFeature(UUID featureId, String owner);

    Mono<Page<Feature>> getAllFeatures(Integer page, Integer size, String sort);

    Mono<UUID> createFeature(FeatureCreateRequest featureRequest);

    Mono<Feature> getById(String id, IdType idType, UUID environmentId);

    Mono<Void> deleteById(UUID id);

    Flux<FeatureStrategyResponseInner> getAllFeatureStrategies();

    Mono<Feature> getFeatureByNameAndEnvironmentId(String name, UUID environmentId);

    Mono<Void> updateFeature(UUID id, FeatureConfiguration configuration);

    Mono<Void> updateFeatureStatus(UUID id, boolean enabled);

    Mono<FeaturePromotionResponse> propagateFeature(UUID id, FeaturePromotionRequest request);

    Flux<PropagationHistory> getPropagationHistory(UUID id);
}
