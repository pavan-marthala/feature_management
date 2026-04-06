# Boolean Evaluation Strategy

The simplest and most common strategy for feature toggling.

## 📝 Description
The **BooleanFeatureStrategy** evaluates features based on a literal `true` or `false` value. It is best used for permanent toggles, kill switches, or manual releases.

## ✅ When to Use
-   **Kill Switches**: Disable a buggy feature instantly across all users.
-   **Manual Releases**: Turn on a feature for everyone at a specific launch time.
-   **Static Toggles**: Toggle infrastructure or legacy code branches.

## ⚙️ Example Configuration
```json
{
  "strategy": "BooleanFeatureStrategy",
  "value": true
}
```

## ⚖️ Pros and Cons
| Pros | Cons |
| :--- | :--- |
| Simple to configure and understand. | No targeting or gradual rollout support. |
| Minimal overhead across SDKs. | All users see the same state (global toggle). |
| Extremely performant evaluation. | Requires manual intervention for changes. |
