import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/pages/DashboardPage.vue'), meta: { title: '仪表盘' } },
      { path: 'users', component: () => import('@/pages/UserListPage.vue'), meta: { title: '用户管理' } },
      { path: 'billing', component: () => import('@/pages/BillingPage.vue'), meta: { title: '点数管理' } },
      { path: 'ai/models', component: () => import('@/pages/ModelConfigPage.vue'), meta: { title: '模型配置' } },
      { path: 'ai/prompts', component: () => import('@/pages/PromptConfigPage.vue'), meta: { title: 'Prompt 配置' } },
      { path: 'templates', component: () => import('@/pages/TemplateManagePage.vue'), meta: { title: '模板管理' } },
    ]
  },
  { path: '/login', component: () => import('@/pages/LoginPage.vue'), meta: { title: '登录' } },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
