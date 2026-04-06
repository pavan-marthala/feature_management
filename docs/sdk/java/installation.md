# Java SDK Installation

Add the Feature Management SDK to your project to start toggling features with ease.

## 📦 Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.feature.management</groupId>
    <artifactId>feature-management-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🛠 Prerequisites

-   **Java**: 17 or higher (Java 25 recommended for full compatibility).
-   **Spring Boot**: 3.x or 4.x (WebFlux support).
-   **AspectJ**: Required for AOP-based toggling.

## 🔄 Automatic Configuration

If you are using Spring Boot, the SDK's `FeatureAutoConfiguration` will automatically register the necessary beans for feature evaluation.

### manual bean registration (Optional)
If not using auto-configuration, register the `FeatureClient` manually:

```java
@Configuration
public class FeatureConfig {
    @Bean
    public FeatureClient featureClient() {
        return new FeatureClient("http://localhost:8080");
    }
}
```
