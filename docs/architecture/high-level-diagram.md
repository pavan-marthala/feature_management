# High-Level Architecture Diagram

The diagram below illustrates the relationship between the dashboard, API, and client SDKs.

```mermaid
graph TD
    %% User/Admin Interaction
    User((User/Admin)) -->|Manage Features| Dashboard[Vue.js Dashboard]
    Dashboard -->|REST API| FM_API[Feature Management API]

    %% Database
    subgraph "Data Layer"
        FM_API -->|Liquibase/R2DBC| Postgres[(PostgreSQL)]
    end

    %% SDK Evaluation Flow
    subgraph "Client Applications"
        JavaApp[Java Application]
        VueApp[Vue.jsx Application]
    end

    JavaApp -->|SDK Interface| JavaSDK[Java SDK]
    JavaSDK -->|Evaluates| FeatureProxy[AOP Aspect Proxy]
    FeatureProxy -->|REST Call| FM_API

    VueApp -->|Client Interactor| VueSDK[Vue Client]
    VueSDK -->|REST Call| FM_API

    %% Response Flow
    FM_API -->|Result: Enabled/Disabled| JavaSDK
    FM_API -->|Result: Feature Set| VueSDK
```

## 🔄 Interaction Flow

### 1. Feature Management
The Admin uses the **Vue.js Dashboard** to create, configure, and monitor features. All management operations are handled via a REST API on the **Feature Management API**.

### 2. Feature Evaluation
Client applications evaluate features through their respective SDKs. The **Java SDK** uses Aspect-Oriented Programming (AOP) to intercept method calls annotated with `@FeatureEnabled`, making evaluation transparent to the developer.

### 3. Data Persistence
All configurations and rules are stored in a **PostgreSQL** database. The API uses **Liquibase** for schema management and **R2DBC** for reactive data access.
