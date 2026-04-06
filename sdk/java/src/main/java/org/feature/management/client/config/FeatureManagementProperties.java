package org.feature.management.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "feature-management")
public class FeatureManagementProperties {
    /**
     * Base URL of the Feature Management API (e.g. http://localhost:8080)
     */
    private String apiUrl;

    /**
     * Retry configuration for API calls.
     */
    private RetryProperties retry = new RetryProperties();

    @Data
    public static class RetryProperties {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private long backoffIntervalMillis = 1000;
    }
}
