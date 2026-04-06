# SDKs Overview

The Feature Management platform provides officially supported SDKs to simplify integration and ensure feature evaluations are accurate and performant.

## 🎯 Why Use an SDK?

| Feature | Direct API usage | Feature Management SDK |
| :--- | :--- | :--- |
| **Simplicity** | Requires manual HTTP requests. | Native language interfaces (Aspects, Hooks). |
| **Performance** | Network latency for every call. | Integrated caching and optimized evaluation. |
| **Reliability** | Manual error handling. | Built-in fallbacks and retry logic. |
| **Code Hygiene** | Clutters business logic. | Declarative toggles (Annotations). |

## 📦 Supported SDKs

### ☕ Java SDK (sdk/java/)
Optimized for Spring Boot and general Java applications. Uses **Aspect-Oriented Programming (AOP)** for transparent method-level feature toggling.
- **Key usage**: `@FeatureEnabled("FEATURE_KEY")`.
- **Target**: Server-side microservices.

## 🖥 Management UI (Dashboard)
To manage and configure features, environments, and strategies, check the [Management Dashboard Guide](file:///Users/pavankalyan/Feature%20Management/docs/dashboard/setup.md).
The dashboard is a standalone Vue.js interface and is not intended to be integrated as a package into external applications.

## 🚀 Evaluation Architecture

SDKs interact with the **Feature Management API** to fetch evaluation results. They are designed to minimize network traffic by supporting bulk evaluations and integrated caching of results.
