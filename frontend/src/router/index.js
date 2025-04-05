import { createRouter, createWebHistory } from 'vue-router'

// 布局组件
const Layout = () => import('@/layout/index.vue')

// 路由配置
const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/data',
    component: Layout,
    redirect: '/data/management',
    name: 'Data',
    meta: { title: '数据管理', icon: 'DataLine' },
    children: [
      {
        path: 'management',
        name: 'DataManagement',
        component: () => import('@/views/data/management/index.vue'),
        meta: { title: '数据管理', icon: 'Document' }
      },
      {
        path: 'analysis',
        name: 'DataAnalysis',
        component: () => import('@/views/data/analysis/index.vue'),
        meta: { title: '数据分析', icon: 'TrendCharts' }
      }
    ]
  },
  {
    path: '/monitor',
    component: Layout,
    redirect: '/monitor/system',
    name: 'Monitor',
    meta: { title: '实时监控', icon: 'Monitor' },
    children: [
      {
        path: 'system',
        name: 'SystemMonitor',
        component: () => import('@/views/monitor/system/index.vue'),
        meta: { title: '系统监控', icon: 'Cpu' }
      },
      {
        path: 'data',
        name: 'DataMonitor',
        component: () => import('@/views/monitor/data/index.vue'),
        meta: { title: '数据监控', icon: 'DataAnalysis' }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    name: 'User',
    meta: { title: '用户管理', icon: 'User' },
    children: [
      {
        path: 'list',
        name: 'UserList',
        component: () => import('@/views/user/list/index.vue'),
        meta: { title: '用户列表', icon: 'UserFilled' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/user/role/index.vue'),
        meta: { title: '角色管理', icon: 'Lock' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 获取token
  const token = localStorage.getItem('token')
  const store = router.app._context.provides.store
  
  // 白名单路径，不需要登录就可以访问
  const whiteList = ['/login', '/register', '/forget-password']
  
  // 如果在白名单中，直接放行
  if (whiteList.includes(to.path)) {
    // 如果已登录且访问登录页，重定向到首页
    if (token && to.path === '/login') {
      next({ path: '/' })
    } else {
      next()
    }
    return
  }
  
  // 如果没有token，重定向到登录页
  if (!token) {
    next(`/login?redirect=${to.path}`)
    return
  }
  
  try {
    // 验证token有效性
    await store.dispatch('user/validateToken')
    
    // 如果没有用户信息，获取用户信息
    if (!store.state.user.userInfo.id) {
      await store.dispatch('user/getUserInfo')
    }
    
    // 检查是否有权限访问该路由
    if (to.meta && to.meta.roles) {
      const hasPermission = store.state.user.roles.some(role => to.meta.roles.includes(role))
      if (!hasPermission) {
        next({ path: '/403' })
        return
      }
    }
    
    // 有效token，放行
    next()
  } catch (error) {
    console.error('路由守卫验证失败:', error)
    // 清除用户信息
    store.commit('user/CLEAR_USER')
    // 重定向到登录页
    next(`/login?redirect=${to.path}`)
  }
})

export default router