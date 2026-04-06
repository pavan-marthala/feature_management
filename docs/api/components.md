# API Component Architecture

An in-depth look at the internal structure of the Feature Management server.

## 🧱 Controllers (org.feature.management.api)

### 🗺 EnvironmentController
-   **createEnvironment(EnvironmentRequest)**: Create new logic environments (e.g., DEV, PROD).
-   **getEnvironmentById(UUID)**: Retrieve environment metadata.
-   **updateEnvironment(UUID, EnvironmentRequest)**: Modify existing environment configurations.
-   **deleteEnvironment(UUID)**: Remove and sunset environments.

### 🚩 FeatureController
-   **createFeature(FeatureRequest)**: Initialize new feature toggles with metadata.
-   **getFeatureById(UUID)**: Retrieve current feature configuration.
-   **updateFeature(UUID, FeatureRequest)**: Change strategies (e.g., Boolean to Role-Based).
-   **updateFeatureStatus(UUID, status)**: Quick global toggle (Enabled/Disabled).
-   **deleteFeature(UUID)**: Permanently remove feature flags.

---

## 🛠 Services (org.feature.management.service)

### 🚩 FeatureService
The core evaluation engine.
-   **evaluate(featureKey, context)**: Identifies the active strategy and applies rules against current context.
-   **save(featureEntity)**: Persists feature definitions.
-   **findAll()**: Returns all active features for the dashboard.

### 🏛 EnvironmentService
Manages logical groupings and ownership across Feature flags.

---

## 💾 Persistence (org.feature.management.repository)

### 📦 R2DBC Repositories
-   **FeatureRepository**: Reactive data access for `feature` table.
-   **EnvironmentRepository**: Reactive data access for `environment` table.

### 🚀 Schema Management (Liquibase)
-   Managed via `db.changelog-master.yaml`.
-   Supports sequential rollbacks and versioned schema updates.

---

## 🛡️ Utilities & Shared (org.feature.management.shared)

### 🧠 Exception Handling (CustomExceptionHandler)
-   Provides consistent error response envelopes.
-   Proper HTTP Status Mapping (e.g., 404 for missing features).

### 🏷 ETags (ETaggableEntity)
-   Enables optimistic concurrency control.
-   Prevents "mid-air collisions" when multiple users edit the same feature.
