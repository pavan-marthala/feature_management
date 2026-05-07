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

    { path: '/workspaces/:workspaceId', redirect: '/features' },
    { path: '/workspaces/:workspaceId/features', redirect: '/features' },
    { path: '/workspaces/:workspaceId/features/:id', redirect: to => `/features/${to.params.id}` },

    {
      path: '/onboarding',
      name: 'onboarding',
      component: () => import('@/views/OnboardingView.vue'),
      meta: { title: 'Get Started', hideLayout: true },
    },

    // Workspace (mostly handled via modal now, but keeping list just in case, though usually global)
    {
      path: '/features',
      name: 'features',
      component: () => import('@/views/FeaturesListView.vue'),
      meta: { title: 'Features' },
    },
    {
      path: '/features/:id',
      name: 'feature-detail',
      component: () => import('@/views/FeatureDetailView.vue'),
      meta: { title: 'Feature Detail' },
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
      path: '/environments/:id',
      name: 'environment-detail',
      component: () => import('@/views/EnvironmentDetailView.vue'),
      meta: { title: 'Environment Detail' },
    },
  ],
})

export default router
