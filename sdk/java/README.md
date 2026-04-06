# Feature Management Client SDK

The **Feature Management Client SDK** is a reusable Spring Boot Starter library designed to abstract all interactions with the core Feature Management API. It enables developers to implement dynamic feature toggles seamlessly, keeping applications fast, scalable, and devoid of repetitive WebClient boilerplate.

## 🚀 Key Features

- **Zero Boilerplate:** Annotation-driven (`@FeatureEnabled`) feature toggling via declarative Aspect-Oriented Programming (AOP).
- **Reactive Native:** Fully non-blocking Architecture built exclusively for Spring WebFlux (`Mono`, `Flux`).
- **Resilient & Fault Tolerant:** Native Retry configurations to gracefully mitigate transient network faults.
- **Auto Configured:** Complete plug-and-play Spring Boot integration. All beans map identically out-of-the-box.
- **Extensible Evaluation:** Highly localized evaluation layer (e.g., `BooleanFeatureStrategy`) natively parsed from the remote OpenAPI configurations.

## 📦 Getting Started

### 1. Add the Dependency

Add the client SDK to your application's `pom.xml`.

```xml
<dependency>
    <groupId>org.feature.management</groupId>
    <artifactId>client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Configure application.yml

Specify the backend Feature Management API properties in your consuming application's config file.

```yaml
feature-management:
  api-url: http://localhost:8080   # Default URL of the Feature Management API
  retry:
    enabled: true                  # Enable resilient retries on 5xx Server failures
    max-attempts: 3                # Maximum retry calls
    backoff-interval-millis: 1000  # Staggered interval down-time in milliseconds
```

## 🛠 Usage & Implementation

Instead of instantiating `WebClient` everywhere or writing nested `if-else` feature evaluation blocks, simply apply the `@FeatureEnabled` annotation directly onto your reactive endpoints or repository methods.

### Example: Flux / Mono

```java
import org.feature.management.client.annotation.FeatureEnabled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController {

    @GetMapping("/checkout")
    @FeatureEnabled("NEW_CHECKOUT") // <--- Declarative check!
    public Mono<String> processPayment() {
        // This method executes ONLY if the 'NEW_CHECKOUT' feature is verified and enabled
        return Mono.just("Checkout processed successfully!");
    }
}
```

If the SDK evaluates `"NEW_CHECKOUT"` and it returns `false` (or is restricted), it intercepts the execution pipeline and immediately halts it by raising a `FeatureDisabledException` down the reactive stream.

> **Note:** Because this library enforces a strictly non-blocking architecture, the annotation is **only permitted on methods returning `Mono` or `Flux`**. Tagging a synchronous method (e.g., returning `String` or `void`) will result in a runtime `FeatureManagementException`.

## ⚙️ How it Works under the Hood

1. **AOP Interception:** The SDK registers a Spring Aspect (`FeatureAspect`) that securely proxies operations containing `@FeatureEnabled`.
2. **Context Payload Mapping:** The `EvaluationContextExtractor` is responsible for building a stateless execution payload locally dynamically checking traits like user roles and headers (customizable per integration).
3. **Reactive Fetching & Local Evaluation:** `FeatureClient` fetches the required `Feature` constraints synchronously via WebClient and pipes it cleanly to the `FeatureEvaluationManager` where local, rapid determination logic is calculated over defined schemas like `BooleanFeatureStrategy`.

## 🛡️ Exceptions & Error Handling

To preserve clean propagation, the SDK can emit the following custom exceptions which you can resolve gracefully via your application's native `@ControllerAdvice` standard `ExceptionHandler`:

- **`FeatureDisabledException`**: Emitted properly when a queried feature evaluates to disabled/false.
- **`FeatureManagementException`**: Thrown for programmatic faults, such as illegally annotating a blocking method, or critical errors communicating with the backing remote server.
