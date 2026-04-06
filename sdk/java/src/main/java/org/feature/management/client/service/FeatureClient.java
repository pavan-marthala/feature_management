package org.feature.management.client.service;

import org.feature.management.client.exception.FeatureManagementException;
import org.feature.management.models.Feature;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.feature.management.client.config.FeatureManagementProperties;
import reactor.util.retry.Retry;
import java.time.Duration;

public class FeatureClient {

    private final WebClient webClient;
    private final String apiUrl;
    private final FeatureManagementProperties.RetryProperties retryProps;

    public FeatureClient(WebClient webClient, FeatureManagementProperties properties) {
        this.webClient = webClient;
        this.apiUrl = properties.getApiUrl() != null ? properties.getApiUrl() : "http://localhost:8080";
        this.retryProps = properties.getRetry();
    }

    public Mono<Feature> fetchFeature(String featureName) {
        Mono<Feature> request = webClient.get()
                .uri(apiUrl + "/features/{id}?idType=NAME", featureName)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() && !status.equals(HttpStatus.NOT_FOUND),
                        _ -> Mono.error(new FeatureManagementException("Client error fetching feature details", null)))
                .onStatus(HttpStatusCode::is5xxServerError,
                        _ -> Mono.error(new FeatureManagementException("Server error fetching feature details", null)))
                .bodyToMono(Feature.class);

        if (retryProps != null && retryProps.isEnabled()) {
            request = request.retryWhen(Retry.backoff(retryProps.getMaxAttempts(), Duration.ofMillis(retryProps.getBackoffIntervalMillis()))
                    .filter(throwable -> {
                        if (throwable instanceof FeatureManagementException) {
                            return throwable.getMessage() != null && throwable.getMessage().contains("Server error");
                        }
                        return true;
                    }));
        }

        return request.onErrorResume(e -> Mono.error(new FeatureManagementException("Error executing request to Feature API", e)));
    }
}
