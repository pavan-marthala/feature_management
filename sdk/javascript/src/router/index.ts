import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { title: 'Dashboard' },
    },
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
