# Vue SDK Usage Guide

The Vue SDK provides a clean and reactive way to toggle features in your front-end application using the **Composition API**.

## 🏷 Basic Usage (`useFeature`)

Use the `useFeature` hook to reactive evaluate feature status in any component.

```vue
<script setup lang="ts">
import { useFeature } from '@feature-management/vue-sdk';

const { isEnabled, isLoading } = useFeature('NEW_HEADER');
</script>

<template>
  <div v-if="isLoading">Loading feature...</div>
  <div v-else>
    <NewHeader v-if="isEnabled" />
    <OldHeader v-else />
  </div>
</template>
```

### ⚙️ How it Works
1.  **Context Registration**: The hook registers the interest in `"NEW_HEADER"`.
2.  **Evaluate**: It calls the Feature Management API and caches the result locally.
3.  **React**: As the evaluation result changes (e.g., via real-time WebSocket or manual refresh), `isEnabled` reactively updates.

---

## 🛡️ Functional Usage (`featureClient`)

For utility functions or complex routing logic, use the `featureClient` directly.

```typescript
import { featureClient } from '@feature-management/vue-sdk';

const checkBetaAccess = async () => {
    const isPublic = await featureClient.isEnabled('PUBLIC_BETA');
    if (isPublic) {
        // ...
    }
}
```

## ✅ Best Practices
-   **Bulk Loading**: The client naturally groups evaluation requests to minimize network roundtrips.
-   **Loading States**: Always handle `isLoading` to prevent UI flickering.
-   **Error Boundaries**: Provide default UI if evaluation fails (e.g., API is offline).
-   **Scoped Components**: Keep feature-dependent logic inside smaller, targeted components for better testability.
