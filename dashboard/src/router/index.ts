import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/features',
    },
    // Legacy redirects
    { path: '/dashboard', redirect: '/features' },
    { path: '/workspaces', redirect: '/features' },

    // Legacy workspace-scoped redirects (graceful migration)
    { path: '/workspaces/:workspaceId', redirect: '/features' },
    { path: '/workspaces/:workspaceId/features', redirect: '/features' },
    { path: '/workspaces/:workspaceId/features/create', redirect: '/features/create' },
    { path: '/workspaces/:workspaceId/features/:id', redirect: to => `/features/${to.params.id}` },
    { path: '/workspaces/:workspaceId/features/:id/edit', redirect: to => `/features/${to.params.id}/edit` },

    // Onboarding wizard (full-screen, no sidebar)
    {
      path: '/onboarding',
      name: 'onboarding',
      component: () => import('@/views/OnboardingView.vue'),
      meta: { title: 'Get Started', hideLayout: true },
    },

    // Workspace create (standalone page, rarely used)
    {
      path: '/workspaces/create',
      name: 'workspace-create',
      component: () => import('@/views/WorkspaceFormView.vue'),
      meta: { title: 'Create Workspace' },
    },

    // Features (flat routes — workspace context from store)
    {
      path: '/features',
      name: 'features',
      component: () => import('@/views/FeaturesListView.vue'),
      meta: { title: 'Features' },
    },
    {
      path: '/features/create',
      name: 'feature-create',
      component: () => import('@/views/FeatureFormView.vue'),
      meta: { title: 'Create Feature' },
    },
    {
      path: '/features/:id',
      name: 'feature-detail',
      component: () => import('@/views/FeatureDetailView.vue'),
      meta: { title: 'Feature Detail' },
    },
    {
      path: '/features/:id/edit',
      name: 'feature-edit',
      component: () => import('@/views/FeatureFormView.vue'),
      meta: { title: 'Edit Feature' },
    },

    // Global workflow routes
    {
      path: '/workflows',
      name: 'workflows',
      component: () => import('@/views/WorkflowsListView.vue'),
      meta: { title: 'Workflows' },
    },
    {
      path: '/workflows/:workflowId',
      name: 'workflow-detail',
      component: () => import('@/views/WorkflowDetailView.vue'),
      meta: { title: 'Workflow Detail' },
    },

    // Global environment routes
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
