import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/workspaces',
    },
    // Legacy redirects
    { path: '/dashboard', redirect: '/workspaces' },
    { path: '/features', redirect: '/workspaces' },
    { path: '/workflows', redirect: '/workspaces' },

    // Onboarding wizard (full-screen, no sidebar)
    {
      path: '/onboarding',
      name: 'onboarding',
      component: () => import('@/views/OnboardingView.vue'),
      meta: { title: 'Get Started', hideLayout: true },
    },

    // Workspace selector (landing page)
    {
      path: '/workspaces',
      name: 'workspaces',
      component: () => import('@/views/WorkspaceSelectorView.vue'),
      meta: { title: 'Workspaces' },
    },
    {
      path: '/workspaces/create',
      name: 'workspace-create',
      component: () => import('@/views/WorkspaceFormView.vue'),
      meta: { title: 'Create Workspace' },
    },

    // Workspace-scoped routes
    {
      path: '/workspaces/:workspaceId',
      name: 'workspace-dashboard',
      component: () => import('@/views/WorkspaceDashboardView.vue'),
      meta: { title: 'Workspace Dashboard', requiresWorkspace: true },
    },
    {
      path: '/workspaces/:workspaceId/features',
      name: 'workspace-features',
      component: () => import('@/views/FeaturesListView.vue'),
      meta: { title: 'Features', requiresWorkspace: true },
    },
    {
      path: '/workspaces/:workspaceId/features/create',
      name: 'workspace-feature-create',
      component: () => import('@/views/FeatureFormView.vue'),
      meta: { title: 'Create Feature', requiresWorkspace: true },
    },
    {
      path: '/workspaces/:workspaceId/features/:id',
      name: 'workspace-feature-detail',
      component: () => import('@/views/FeatureDetailView.vue'),
      meta: { title: 'Feature Detail', requiresWorkspace: true },
    },
    {
      path: '/workspaces/:workspaceId/features/:id/edit',
      name: 'workspace-feature-edit',
      component: () => import('@/views/FeatureFormView.vue'),
      meta: { title: 'Edit Feature', requiresWorkspace: true },
    },
    {
      path: '/workspaces/:workspaceId/workflow',
      name: 'workspace-workflow',
      component: () => import('@/views/WorkflowDetailView.vue'),
      meta: { title: 'Workflow', requiresWorkspace: true },
    },

    // Global environment routes (unchanged)
    {
      path: '/environments',
      name: 'environments',
      component: () => import('@/views/EnvironmentsView.vue'),
      meta: { title: 'Environments' },
    },
    {
      path: '/environments/create',
      name: 'environment-create',
      component: () => import('@/views/EnvironmentFormView.vue'),
      meta: { title: 'Create Environment' },
    },
    {
      path: '/environments/:id',
      name: 'environment-detail',
      component: () => import('@/views/EnvironmentDetailView.vue'),
      meta: { title: 'Environment Detail' },
    },
    {
      path: '/environments/:id/edit',
      name: 'environment-edit',
      component: () => import('@/views/EnvironmentFormView.vue'),
      meta: { title: 'Edit Environment' },
    },
  ],
})

export default router
