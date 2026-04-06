# User-Based Evaluation Strategy

Evaluate features based on unique identifier(s) of the user.

## 📝 Description
The **UserBasedFeatureStrategy** enables specific features for a curated list of user IDs. Evaluation is precise and deterministic based on the provided user context.

## ✅ When to Use
-   **Canary Testing**: Enable a new feature for internal team members.
-   **Beta testing**: Whitelist specific power users to test early-access releases.
-   **Targeted fixes**: Provide specific fixes or debugging tools for individual users.

## ⚙️ Example Configuration
```json
{
  "strategy": "UserBasedFeatureStrategy",
  "users": ["user-123", "pavan@example.com", "admin-01"]
}
```

## ⚖️ Pros and Cons
| Pros | Cons |
| :--- | :--- |
| Precise targeting of individuals. | Management overhead for large lists. |
| Deterministic evaluation. | Not suitable for dynamic segments. |
| Excellent for internal testing. | Requires consistent user IDs. |
