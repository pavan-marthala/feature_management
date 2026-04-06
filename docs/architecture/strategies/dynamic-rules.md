# Dynamic Evaluation Strategy

Evaluate features based on complex, runtime-resolved conditions.

## 📝 Description
The **ScriptEngineFeatureStrategy** (or Dynamic Rules) enables features based on complex logic that cannot be expressed purely with fixed lists or roles. This allows for fine-grained control based on multiple request inputs.

## ✅ When to Use
-   **Experimental Segments**: Enable features for users with `tier=PRO`, `region=LATAM`, and `signup_date < 2024`.
-   **Header/Request Analysis**: Enable or disable based on browser versions or specific custom headers.
-   **Query-Based Evaluation**: Toggle based on URL parameters for debugging or support.

## ⚙️ Example Configuration (HTTP Request Based)
```json
{
  "strategy": "HTTPRequestFeatureStrategy",
  "header": {
    "name": "X-Beta-Access",
    "value": "enabled-true"
  }
}
```

## ⚖️ Pros and Cons
| Pros | Cons |
| :--- | :--- |
| Ultra-flexible targeting. | Evaluation complexity increases. |
| Complex logic support. | Potential security risks (if script-based). |
| Runtime context aware. | Debugging can be challenging. |
