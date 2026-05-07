<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import { useUiStore } from '@/stores/uiStore'
import { useOnboardingStore } from '@/stores/onboardingStore'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import { useEnvironmentStore } from '@/stores/environmentStore'

const uiStore = useUiStore()
const onboardingStore = useOnboardingStore()
const workspaceStore = useWorkspaceStore()
const environmentStore = useEnvironmentStore()
const route = useRoute()
const router = useRouter()

const hideLayout = computed(() => route.meta.hideLayout === true)

onMounted(async () => {
  uiStore.initTheme()
  onboardingStore.restoreState()

  // Auto-detect new user
  if (!onboardingStore.completed && route.name !== 'onboarding') {
    const isNew = await onboardingStore.checkIfNewUser()
    if (isNew) {
      router.push('/onboarding')
      return
    }
  }

  // Initialize active contexts
  await Promise.all([
    workspaceStore.initActiveWorkspace(),
    environmentStore.initActiveEnvironment()
  ])
})
</script>

<template>
  <DashboardLayout v-if="!hideLayout" />
  <router-view v-else />
  <ToastContainer />
</template>

