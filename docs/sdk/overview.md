# SDKs Overview

The Feature Management platform provides officially supported SDKs to simplify integration and ensure feature evaluations are accurate and performant.

## 🎯 Why Use an SDK?

| Feature | Direct API usage | Feature Management SDK |
| :--- | :--- | :--- |
| **Simplicity** | Requires manual HTTP requests. | Native language interfaces (Aspects, Hooks). |
| **Performance** | Network latency for every call. | Integrated caching and optimized evaluation. |
| **Reliability** | Manual error handling. | Built-in fallbacks and retry logic. |
| **Code Hygiene** | Clutters business logic. | Declarative toggles (Annotations, Components). |

## 📦 Supported SDKs

### ☕ Java SDK (sdk/java/)
Optimized for Spring Boot and general Java applications. Uses **Aspect-Oriented Programming (AOP)** for transparent method-level feature toggling.
- **Key usage**: `@FeatureEnabled("FEATURE_KEY")`.
- **Target**: Server-side microservices.

### 🎨 Vue SDK (sdk/javascript/)
Optimized for modern front-end applications built with Vue.js 3.
- **Key usage**: `useFeature('FEATURE_KEY')` hook.
- **Target**: SPAs and Dashboards.

## 🚀 Evaluation Architecture

SDKs interact with the **Feature Management API** to fetch evaluation results. They are designed to minimize network traffic by supporting bulk evaluations and (optionally) local caching of results.
