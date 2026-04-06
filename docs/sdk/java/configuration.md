# Java SDK Configuration

Configure the Java SDK to communicate with the Feature Management API and define its runtime behavior.

## ⚙️ Properties Configuration

The SDK is configured using the `feature-management` prefix in your `application.yml` or `application.properties`:

```yaml
feature-management:
  api-url: http://localhost:8080   # Base URL of the API
  retry:
    enabled: true                 # Enable retry on evaluation failure
    max-attempts: 3              # Maximum number of retry attempts
    backoff-interval-millis: 1000 # Delay between retries in milliseconds
```

## 🏗 Programmatic Bean Setup

If you prefer custom configuration, you can define your own settings:

```java
@Bean
public FeatureConfig featureConfig() {
    return FeatureConfig.builder()
        .apiUrl("http://localhost:8080")
        .retryEnabled(true)
        .maxAttempts(3)
        .backoffInterval(1000)
        .build();
}
```