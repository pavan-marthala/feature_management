# Getting Started: Setup Guide

Follow this guide to get the Feature Management platform up and running on your local machine.

## 📋 Prerequisites

-   **Docker**: Desktop or Engine 20.x or higher.
-   **Java**: SDK 21 or 25 (if running manually).
-   **Node.js**: 20.x (for UI development).
-   **Maven**: 3.9.x (for building the API).

---

## 🚀 1. Run via Docker Compose (Recommended)

The easiest way to get everything running in a production-like environment:

```bash
git clone https://github.com/your-username/feature-management.git
cd feature-management/infrastructure
docker-compose up -d
```

### 🔗 Accessing the services
-   **Management Dashboard**: [http://localhost:3000](http://localhost:3000)
-   **Core API**: [http://localhost:8080](http://localhost:8080)
-   **Example App**: [http://localhost:8081](http://localhost:8081)

---

## 🛠 2. Manual Setup (Development Mode)

### Step 1: Run PostgreSQL
Ensure you have a PostgreSQL instance running on port `5432` with a database named `feature_management`.

### Step 2: Start the API
```bash
cd server
./mvnw spring-boot:run
```

### Step 3: Start the Dashboard
```bash
cd sdk/javascript
npm install
npm run dev
```

---

## ✅ 3. Verify the Setup

1.  **Dashboard**: Open [http://localhost:3000](http://localhost:3000). You should see the login screen or an empty feature dashboard.
2.  **API Heat-Check**:
    ```bash
    curl http://localhost:8080/dashboard/stats
    ```
    Expected output: `{"totalFeatures":0, "activeFeatures":0, "disabledFeatures":0, "totalEnvironments":0}`.

3.  **Create your first feature**:
    Use the Postman collection in `docs/api/postman_collection.json` to create a `TestFeature`.
