# Java SDK Configuration

Configure the Java SDK to communicate with the Feature Management API and define its runtime behavior.

## ⚙️ Properties Configuration

The SDK can be configured using standard Spring Boot `application.yml` or `application.properties`:

```yaml
feature:
  management:
    api-url: http://localhost:8080   # Base URL of the API
    cache:
      enabled: true                 # Default: true
      ttl: 60                      # Time To Live in seconds
    timeout: 5000                  # Connect/Read timeout in ms
```

## 🏗 Programmatic Bean Setup

If you prefer custom configuration, define a `FeatureConfig` bean:

```java
@Bean
public FeatureConfig featureConfig() {
    return FeatureConfig.builder()
        .apiUrl("http://localhost:8080")
        .cacheEnabled(true)
        .cacheTtl(60)
        .build();
}
```