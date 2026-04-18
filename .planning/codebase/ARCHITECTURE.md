# Architecture
Date: 2026-04-18

## System Design
The system follows a classic decoupled client-server architecture using a reactive, non-blocking paradigm.
- **Server**: A Spring WebFlux application exposing REST APIs.
- **Client (UI)**: A Vue.js Single Page Application (SPA).
- **Client (SDKs)**: Language-specific SDKs (Java, Javascript) that consume the API for feature evaluation.

## Data Flow
1. **Management**: Users configure feature flags and environments via the Vue.js dashboard. This triggers REST calls to the Spring Boot API, which persists changes to PostgreSQL. Optimistic locking is enforced via `etag` fields.
2. **Evaluation**: Applications integrating the SDKs fetch the feature rules via the API. The SDK handles context resolution (e.g., user identity, role) and evaluates feature visibility locally or via the server.

## Database Design
- **Core Entities**: `feature`, `environment`, `feature_environment_mapping`, `workflow`, `stage`.
- Features are mapped to environments, allowing different toggle states across DEV, STAGE, and PROD.
