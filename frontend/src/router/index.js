import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('../views/LayoutView.vue'),
    meta: { requiresAuth: true },
    redirect: '/messages',
    children: [
      {
        path: 'messages',
        name: 'messages',
        component: () => import('../views/message/MessageListView.vue'),
        meta: { title: '最近消息', tab: true }
      },
      {
        path: 'customers',
        name: 'customers',
        component: () => import('../views/customer/CustomerListView.vue'),
        meta: { title: '客户', tab: true }
      },
      {
        path: 'contracts',
        name: 'contracts',
        component: () => import('../views/contract/ContractListView.vue'),
        meta: { title: '合同', tab: true }
      },
      {
        path: 'visits',
        name: 'visits',
        component: () => import('../views/visit/VisitListView.vue'),
        meta: { title: '拜访记录', tab: true }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/profile/ProfileView.vue'),
        meta: { title: '个人', tab: true }
      }
    ]
  },
  {
    path: '/customers/:id',
    name: 'customer-detail',
    component: () => import('../views/customer/CustomerDetailView.vue'),
    meta: { requiresAuth: true, title: '客户详情' }
  },
  {
    path: '/customers/:id/edit',
    name: 'customer-edit',
    component: () => import('../views/customer/CustomerEditView.vue'),
    meta: { requiresAuth: true, title: '修改客户' }
  },
  {
    path: '/customers/:id/project/add',
    name: 'project-add',
    component: () => import('../views/customer/ProjectAddView.vue'),
    meta: { requiresAuth: true, title: '录入项目' }
  },
  {
    path: '/customers/:customerId/visit/add',
    name: 'visit-add',
    component: () => import('../views/visit/VisitFormView.vue'),
    meta: { requiresAuth: true, title: '添加拜访记录' }
  },
  {
    path: '/customers/:customerId/visits',
    name: 'customer-visits',
    component: () => import('../views/visit/VisitListView.vue'),
    meta: { requiresAuth: true, title: '拜访记录' }
  },
  {
    path: '/visits/:id',
    name: 'visit-detail',
    component: () => import('../views/visit/VisitDetailView.vue'),
    meta: { requiresAuth: true, title: '拜访详情' }
  },
  {
    path: '/customers/:customerId/contract/add',
    name: 'contract-add',
    component: () => import('../views/contract/ContractFormView.vue'),
    meta: { requiresAuth: true, title: '添加合同' }
  },
  {
    path: '/customers/:customerId/contracts',
    name: 'customer-contracts',
    component: () => import('../views/contract/ContractListView.vue'),
    meta: { requiresAuth: true, title: '合同列表' }
  },
  {
    path: '/contracts/:id',
    name: 'contract-detail',
    component: () => import('../views/contract/ContractDetailView.vue'),
    meta: { requiresAuth: true, title: '合同详情' }
  },
  {
    path: '/profile/edit',
    name: 'profile-edit',
    component: () => import('../views/profile/ProfileEditView.vue'),
    meta: { requiresAuth: true, title: '修改个人信息' }
  },
  {
    path: '/profile/password',
    name: 'change-password',
    component: () => import('../views/profile/ChangePasswordView.vue'),
    meta: { requiresAuth: true, title: '修改密码' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/messages'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} - CustomerCollection` : 'CustomerCollection'
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    return '/messages'
  }
  return true
})

export default router
