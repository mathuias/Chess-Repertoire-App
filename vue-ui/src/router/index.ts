import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/openings',
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/openings',
      name: 'openings',
      component: () => import('@/views/OpeningsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/openings/new',
      name: 'opening-new',
      component: () => import('@/views/OpeningEditorView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/openings/:id(\\d+)',
      name: 'opening-edit',
      component: () => import('@/views/OpeningEditorView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/hello',
      name: 'hello',
      component: () => import('@/views/HelloView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'openings' }
  }
})

export default router
