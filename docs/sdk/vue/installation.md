# Vue SDK Installation

The Vue SDK (contained in `sdk/javascript`) is optimized for modern Vue 3 applications.

## 📦 NPM Installation

```bash
cd sdk/javascript
npm install
```

## 🛠 Prerequisites

-   **Vue.js**: 3.x (Composition API).
-   **Vite**: Recommended build tool.
-   **Axios**: Core HTTP client for API communication.
-   **Pinia**: Recommended state management.

## 🔄 Project Setup

To use the Vue SDK, initialize the `FeatureProvider` in your `main.ts`:

```javascript
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { FeatureProvider } from '@feature-management/vue-sdk';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(FeatureProvider, {
  apiUrl: 'http://localhost:8080'
});

app.mount('#app');
```
