# API Documentation Overview

The Feature Management API is the central coordinator for feature flag configuration and evaluation. It follows a reactive, non-blocking design using Spring WebFlux.

## 🎯 Purpose

-   **Management**: CRUD operations for features and environments through restricted endpoints.
-   **Evaluation**: High-performance, real-time evaluation of feature statuses for diverse clients.
-   **Visibility**: Real-time status, audit metadata (ETags), and dashboard statistics.

## 🔄 Feature Evaluation Flow

1.  **Request**: Client (SDK or UI) sends a request to `/features/{id}/status` or a bulk evaluation endpoint.
2.  **Context Extraction**: The API extracts evaluation context (e.g., JWT claims, headers, query params).
3.  **Strategy Resolution**: The API identifies the feature's core strategy (Boolean, JWT, Schedule, etc.).
4.  **Rule Execution**: The strategy-specific logic evaluates the feature based on the context.
5.  **Result Delivery**: Returns a JSON response with the feature status (Enabled/Disabled).

## 🛡️ Authentication & Authorization

While currently designed for internal usage, the API is structured to support JWT-based authentication. Features can be restricted to specific owners, ensuring that only authorized users can toggle critical flags.

## 📦 Base Configuration

-   **Base URL**: `http://localhost:8080` (or `http://localhost:8080/api/` if using the Nginx proxy).
-   **Port**: 8080.
-   **Encoding**: UTF-8.
-   **Content-Type**: `application/json`.
