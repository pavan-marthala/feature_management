# Java SDK Usage Guide

The Java SDK provides a declarative and clean way to toggle features in your application using **Aspect-Oriented Programming (AOP)**.

## 🏷 Basic Usage (`@FeatureEnabled`)

Annotate any Spring Component method with `@FeatureEnabled` to automatically evaluate a feature before execution.

```java
@Service
public class CheckoutService {

    @FeatureEnabled("NEW_CHECKOUT_FLOW")
    public Mono<String> processCheckout() {
        return Mono.just("Processing checkout with NEW flow...");
    }

    public Mono<String> fallbackCheckout() {
        return Mono.just("Processing checkout with LEGACY flow...");
    }
}
```

### ⚙️ How it Works
1.  **Intercept**: The `FeatureAspect` intercepts the method call.
2.  **Evaluate**: It calls the Feature Management API with the feature key `"NEW_CHECKOUT_FLOW"`.
3.  **Execute/Block**: If **Enabled**, the method executes. If **Disabled**, it throws a `FeatureDisabledException` (or uses a fallback).

---

## 🛡️ Functional Usage (`FeatureClient`)

For more complex logic where annotations aren't sufficient, use the `FeatureClient` directly.

```java
@Autowired
private FeatureClient featureClient;

public Mono<String> dynamicCheckout() {
    return featureClient.isEnabled("NEW_CHECKOUT_FLOW")
        .flatMap(enabled -> {
            if (enabled) {
                return processNewFlow();
            } else {
                return processLegacyFlow();
            }
        });
}
```

## ✅ Best Practices
-   **Key Consistency**: Use constants for feature keys to avoid typos.
-   **Atomic Methods**: Keep `@FeatureEnabled` methods focused on the code branch being toggled.
-   **Reactive Compatibility**: Always return `Mono` or `Flux` for full reactive performance.
