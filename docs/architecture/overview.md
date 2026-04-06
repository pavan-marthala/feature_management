# Architecture Overview

The Feature Management platform is designed as a high-performance, non-blocking system to evaluate and manage feature flags across diverse environments.

## 🧱 System Components

1.  **Core API (server/)**: A Spring Boot WebFlux service that manages feature rules, environments, and serves evaluation requests.
2.  **Java SDK (sdk/java/)**: A client library that integrates into Java applications using AOP to enable feature toggling at the method level.
3.  **Vue SDK/UI (sdk/javascript/)**: A modern dashboard for managing features and a generic JavaScript client for front-end integration.
4.  **Database (PostgreSQL)**: Stores feature configurations, environment data, and audit logs.

## 🔄 Feature Lifecycle

1.  **Creation**: A feature is defined in the Dashboard with a unique key and an initial strategy (e.g., Boolean).
2.  **Configuration**: Strategies are tuned (e.g., enabling for specific user roles or a schedule).
3.  **Evaluation**: An SDK or external client requests the status of a feature. The API evaluates the rules against the provided context.
4.  **Enforcement**: The application acts based on the evaluation result (enabled/disabled).

## ⚡ Performance & Scalability

- **Non-blocking I/O**: The entire stack uses Project Reactor for high concurrency with minimal resource footprint.
- **R2DBC**: Reactive database connectivity ensures the database driver doesn't block evaluation threads.
- **Caching**: Future-ready for distributed caching (e.g., Redis) to minimize database hits for hot features.
