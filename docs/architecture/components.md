# Component Architecture

Detailed overview of the internal components of the Feature Management platform.

## 📦 API Components (server/)

### 🛂 Controllers
Handle incoming REST requests for feature management, environment configuration, and evaluation.
- **EnvironmentController**: CRUD for environments and owner management.
- **FeatureController**: CRUD for features, status toggling, and configuration updates.
- **DashboardController**: Statistics and reporting.

### 🛠 Services
The core business logic layer.
- **EnvironmentService**: Validates and manages environment lifecycles.
- **FeatureService**: Implements rule evaluation logic and configuration persistence.
- **Mapper (FeatureMapper)**: Converts between DTOs and database Entities using MapStruct.

### 💾 Persistence
- **R2DBC Repositories**: Non-blocking data access for PostgreSQL.
- **Liquibase**: Sequential database migrations for scheme evolution.

---

## ☕ Java SDK Components (sdk/java/)

### 🏷 Annotations
Custom annotations like `@FeatureEnabled` for declarative feature toggling.

### 🛡 Aspect (FeatureAspect)
An AspectJ-based interceptor that wraps method execution with feature evaluation logic before allowing the call to proceed.

### 📡 Client (FeatureClient)
The low-level HTTP client (using Spring WebClient) that communicates with the API to fetch feature status.

### 🧠 Cache (Optional)
In-memory caching of feature evaluations to minimize network latency for frequently accessed flags.

---

## 🎨 UI & Web Client (sdk/javascript/ - documented as "Vue SDK")

### 📱 Components
- **Dashboard**: Central management UI for features and environments.
- **Strategy Editors**: Dedicated UI components for configuring Boolean, JWT, and Scheduled strategies.

### 🔗 Services
- **Axios Integration**: Pre-configured HTTP client for authenticated communication with the API.
- **State Management (Pinia)**: Real-time synchronization of feature states across the UI.
