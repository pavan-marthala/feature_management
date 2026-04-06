# Vue SDK Configuration

Configure the Vue.js client to communicate with the Feature Management API and define its runtime behavior.

## ⚙️ Configuration Properties

The SDK can be configured using standard `vite.env` or reactive properties:

```typescript
const featureConfig = {
    apiUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080',
    timeout: 5000,              // Request timeout in ms
    cache: {
        enabled: true,          // Default: true
        ttl: 60                 // Time To Live in seconds
    },
    auth: {
        token: localStorage.getItem('jwt_token')
    }
};

// pass the config to Provider
app.use(FeatureProvider, featureConfig);
```

## 🏗 Axios Interceptor Integration

If your application already uses **Axios**, the SDK can be configured to use your existing instance:

```typescript
import axios from 'axios';
import { FeatureProvider } from '@feature-management/vue-sdk';

const myAxiosInstance = axios.create({
    baseURL: 'http://localhost:8080'
});

app.use(FeatureProvider, {
    axiosInstance: myAxiosInstance
});
```
