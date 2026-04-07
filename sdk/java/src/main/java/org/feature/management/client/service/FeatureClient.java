package org.feature.management.client.service;

import org.feature.management.client.exception.FeatureManagementException;
import org.feature.management.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.feature.management.client.config.FeatureManagementProperties;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.UUID;

public class FeatureClient {

    private final WebClient webClient;
    private final String apiUrl;
    private final FeatureManagementProperties.RetryProperties retryProps;

    public FeatureClient(WebClient webClient, FeatureManagementProperties properties) {
        this.webClient = webClient;
        this.apiUrl = properties.getApiUrl() != null ? properties.getApiUrl() : "http://localhost:8080";
        this.retryProps = properties.getRetry();
    }

    /**
     * Helper for generic request execution with retry logic and error handling.
     */
    private <T> Mono<T> executeRequest(WebClient.ResponseSpec responseSpec, Class<T> responseType,
            String errorMessage) {
        Mono<T> processedRequest = responseSpec
                .onStatus(status -> status.is4xxClientError() && !status.equals(HttpStatus.NOT_FOUND),
                        _ -> Mono.error(new FeatureManagementException("Client error: " + errorMessage, null)))
                .onStatus(HttpStatusCode::is5xxServerError,
                        _ -> Mono.error(new FeatureManagementException("Server error: " + errorMessage, null)))
                .bodyToMono(responseType);

        if (retryProps != null && retryProps.isEnabled()) {
            processedRequest = processedRequest.retryWhen(
                    Retry.backoff(retryProps.getMaxAttempts(), Duration.ofMillis(retryProps.getBackoffIntervalMillis()))
                            .filter(throwable -> {
                                if (throwable instanceof FeatureManagementException) {
                                    return throwable.getMessage() != null
                                            && throwable.getMessage().contains("Server error");
                                }
                                return true;
                            }));
        }

        return processedRequest.onErrorResume(e -> {
            if (e instanceof FeatureManagementException) {
                return Mono.error(e);
            }
            return Mono.error(new FeatureManagementException("Error executing request: " + errorMessage, e));
        });
    }

    public Mono<Feature> fetchFeature(String identifier) {
        return fetchFeature(identifier, IdType.NAME);
    }

    public Mono<Feature> fetchFeature(String identifier, IdType idType) {
        return executeRequest(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(apiUrl + "/features/{id}")
                                .queryParam("idType", idType.getValue())
                                .build(identifier))
                        .retrieve(),
                Feature.class,
                "fetching feature details");
    }

    public Mono<UUID> createFeature(Feature feature) {
        return executeRequest(
                webClient.post()
                        .uri(apiUrl + "/features")
                        .bodyValue(feature)
                        .retrieve(),
                UUID.class,
                "creating feature");
    }

    public Mono<FeatureResponse> getAllFeatures(Integer page, Integer size) {
        return executeRequest(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(apiUrl + "/features")
                                .queryParam("page", page)
                                .queryParam("size", size)
                                .build())
                        .retrieve(),
                FeatureResponse.class,
                "fetching all features");
    }

    public Mono<Void> updateFeature(String id, FeatureConfiguration configuration, Integer ifMatch) {
        return executeRequest(
                webClient.patch()
                        .uri(apiUrl + "/features/{id}", id)
                        .header("If-Match", String.valueOf(ifMatch))
                        .bodyValue(configuration)
                        .retrieve(),
                Void.class,
                "updating feature");
    }

    public Mono<Void> deleteFeature(String id, Integer ifMatch) {
        return executeRequest(
                webClient.delete()
                        .uri(apiUrl + "/features/{id}", id)
                        .header("If-Match", String.valueOf(ifMatch))
                        .retrieve(),
                Void.class,
                "deleting feature");
    }

    public Mono<Void> updateFeatureStatus(String id, boolean enabled) {
        return executeRequest(
                webClient.patch()
                        .uri(uriBuilder -> uriBuilder
                                .path(apiUrl + "/features/{id}/status")
                                .queryParam("status", enabled)
                                .build(id))
                        .retrieve(),
                Void.class,
                "updating feature status");
    }

    public Flux<FeatureStrategyResponseInner> getStrategyTypes() {
        return webClient.get()
                .uri(apiUrl + "/features/strategies")
                .retrieve()
                .bodyToFlux(FeatureStrategyResponseInner.class);
    }

    public Mono<UUID> createEnvironment(EnvironmentRequest request) {
        return executeRequest(
                webClient.post()
                        .uri(apiUrl + "/environments")
                        .bodyValue(request)
                        .retrieve(),
                UUID.class,
                "creating environment");
    }

    public Mono<EnvironmentResponse> getAllEnvironments(Integer page, Integer size) {
        return executeRequest(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(apiUrl + "/environments")
                                .queryParam("page", page)
                                .queryParam("size", size)
                                .build())
                        .retrieve(),
                EnvironmentResponse.class,
                "fetching all environments");
    }

    public Mono<Environment> getEnvironment(String id) {
        return executeRequest(
                webClient.get()
                        .uri(apiUrl + "/environments/{id}", id)
                        .retrieve(),
                Environment.class,
                "fetching environment details");
    }

    public Mono<Void> updateEnvironment(String id, EnvironmentRequest request, Integer ifMatch) {
        return executeRequest(
                webClient.patch()
                        .uri(apiUrl + "/environments/{id}", id)
                        .header("If-Match", String.valueOf(ifMatch))
                        .bodyValue(request)
                        .retrieve(),
                Void.class,
                "updating environment");
    }

    public Mono<Void> deleteEnvironment(String id, Integer ifMatch) {
        return executeRequest(
                webClient.delete()
                        .uri(apiUrl + "/environments/{id}", id)
                        .header("If-Match", String.valueOf(ifMatch))
                        .retrieve(),
                Void.class,
                "deleting environment");
    }

    // --- Dashboard APIs ---

    public Mono<DashboardStats> getDashboardStats() {
        return executeRequest(
                webClient.get()
                        .uri(apiUrl + "/dashboard/stats")
                        .retrieve(),
                DashboardStats.class,
                "fetching dashboard statistics");
    }

    // --- Owner Management ---

    public Mono<Void> addFeatureOwner(String id, String ownerId) {
        return executeRequest(
                webClient.post()
                        .uri(apiUrl + "/features/{id}/owners", id)
                        .bodyValue(ownerId)
                        .retrieve(),
                Void.class,
                "adding feature owner");
    }

    public Mono<Void> removeFeatureOwner(String id, String ownerId) {
        return executeRequest(
                webClient.delete()
                        .uri(apiUrl + "/features/{id}/owners/{ownerId}", id, ownerId)
                        .retrieve(),
                Void.class,
                "removing feature owner");
    }

    public Mono<Void> addEnvironmentOwner(String id, String ownerId) {
        return executeRequest(
                webClient.post()
                        .uri(apiUrl + "/environments/{id}/owners", id)
                        .bodyValue(ownerId)
                        .retrieve(),
                Void.class,
                "adding environment owner");
    }

    public Mono<Void> removeEnvironmentOwner(String id, String ownerId) {
        return executeRequest(
                webClient.delete()
                        .uri(apiUrl + "/environments/{id}/owners/{ownerId}", id, ownerId)
                        .retrieve(),
                Void.class,
                "removing environment owner");
    }
}
