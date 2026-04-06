# Management Dashboard Setup

The Feature Management Dashboard is a standalone Vue.js interface for managing features, environments, and strategies. It is not an SDK for integration, but a tool for operators and admins.

## 📦 Setting Up the UI

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/feature-management.git
    cd sdk/javascript
    ```

2.  **Install Dependencies**:
    ```bash
    npm install
    ```

3.  **Run in Development Mode**:
    ```bash
    npm run dev
    ```

## 🛠 Configuration

The UI uses the following configuration for its connection to the Feature Management API:

```typescript
// Located in your environment configuration (e.g., .env)
VITE_API_URL=http://localhost:8080
```

## 🚀 Deployment

To build the dashboard for production and serve it (e.g., via Nginx):

```bash
npm run build
```
This will generate a `dist/` folder containing the static assets.
