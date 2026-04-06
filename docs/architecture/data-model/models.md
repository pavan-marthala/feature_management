# Data Model Reference

The data model for the Feature Management platform is designed for flexibility and extensibility using JSONB for complex strategy configurations.

## 📦 Core Models

### 🗺 Feature
Represents a single feature flag and its evaluation strategy.
- **id**: (UUID) Unique identifier.
- **name**: (String) Unique feature key (e.g., "new-checkout-flow").
- **description**: (String, optional) Helpful context.
- **enabled**: (Boolean) Master toggle for the feature.
- **configuration**: (JSONB) Strategy-specific configuration.
- **owners**: (Array of Strings) User IDs who can manage the feature.

#### Example (Boolean Feature)
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "BetaHeader",
  "description": "Show the new beta header to users",
  "enabled": true,
  "configuration": {
    "strategy": "BooleanFeatureStrategy",
    "value": true
  },
  "owners": ["admin-user"]
}
```

---

### 🏛 Environment
Logical segregation of features for different stages of the development lifecycle.
- **id**: (UUID) Unique identifier.
- **name**: (String) Environment name (e.g., "Production", "Staging").
- **description**: (String) Optional description.

#### Example
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Production",
  "description": "Main production environment"
}
```

---

### 📐 Strategy Configurations

- **BooleanFeatureStrategy**: Simple true/false evaluation.
- **JWTClaimFeatureStrategy**: Rule-based evaluation using JWT claims (Roles, Scopes).
- **HTTPRequestFeatureStrategy**: Evaluation based on HTTP headers, query params, or body content.
- **ScheduleFeatureStrategy**: Time-based activation using Cron expressions.

### 🔄 Evaluation Request (Context)
Sent by SDKs to provide context for rule evaluation.
- **featureKey**: (String) The feature key to evaluate.
- **context**: (Object) Metadata like user roles, request headers, etc.

#### Example
```json
{
  "featureKey": "new-checkout-flow",
  "context": {
    "roles": ["PREMIUM_USER"],
    "region": "US"
  }
}
```
