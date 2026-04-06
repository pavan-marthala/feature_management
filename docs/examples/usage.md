# Examples: Practical Usage

Practical usage examples for both the **Java SDK** and **API** to help you integrate Feature Management into your platform.

## ☕ 1. Java SDK Method-Level Toggling

In your Spring Boot application, use the `@FeatureEnabled` annotation to toggle controller or service methods.

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @FeatureEnabled("NEW_ORDER_V2")
    @PostMapping("/")
    public Mono<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrderWithV2(request);
    }
}
```

### ⚙️ Evaluation Log (AOP)
When `createOrder` is called, the SDK will log:
```
INFO: Evaluating feature 'NEW_ORDER_V2' for method 'createOrder'
DEBUG: Feature 'NEW_ORDER_V2' is ENABLED. Proceeding...
```

---

## 📡 2. API Curl Examples

For CLI tools or non-Java applications, interact directly with the REST API.

### 🚩 Get Feature Status
```bash
curl -X GET "http://localhost:8080/features/NEW_ORDER_V2/status" \
     -H "Accept: application/json"
```

### 🏛 Create a New Environment
```bash
curl -X POST "http://localhost:8080/environments" \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Staging",
           "description": "Pre-production testing environment"
         }'
```

---

## 🎨 3. UI Flow Explanation

1.  **Dashboard**: All features across all environments are visible on the main landing page.
2.  **Toggle**: Each feature has a master `Enabled/Disabled` switch.
3.  **Strategy Config**: Click the "Edit" icon to change strategies (e.g., from Boolean to Scheduled).
4.  **Owners**: Use the "Owners" tab to add or remove team members who can manage the flag.
