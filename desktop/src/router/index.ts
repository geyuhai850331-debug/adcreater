import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/LoginPage.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/pages/HomePage.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'ad/create',
        name: 'AdCreate',
        component: () => import('@/pages/AdCreatePage.vue'),
        meta: { title: '广告制作' }
      },
      {
        path: 'ad/preview',
        name: 'AdPreview',
        component: () => import('@/pages/AdPreviewPage.vue'),
        meta: { title: '广告预览' }
      },
      {
        path: 'delivery',
        name: 'Delivery',
        component: () => import('@/pages/DeliveryPage.vue'),
        meta: { title: '投放' }
      },
      {
        path: 'templates',
        name: 'Templates',
        component: () => import('@/pages/TemplatesPage.vue'),
        meta: { title: '模板' }
      },
      {
        path: 'resources',
        name: 'Resources',
        component: () => import('@/pages/ResourcesPage.vue'),
        meta: { title: '本地资源' }
      },
      {
        path: 'billing',
        name: 'Billing',
        component: () => import('@/pages/BillingPage.vue'),
        meta: { title: '充值' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// Navigation guard - check auth
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/home')
  } else {
    next()
  }
})

export default router
