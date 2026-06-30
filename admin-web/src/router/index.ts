import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import Login from '../views/login/index.vue'
import { canAccess, hasLoginUser } from '../utils/permission'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
    },
  },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: {
          title: '仪表盘',
        },
      },
      {
        path: 'users',
        component: () => import('../views/users/index.vue'),
        meta: {
          title: '用户管理',
          permissions: ['user:list'],
          roles: ['SUPER_ADMIN', 'ADMIN', 'OPERATOR'],
        },
      },
      {
        path: 'depts',
        component: () => import('../views/depts/index.vue'),
        meta: {
          title: '部门管理',
          permissions: ['dept:list'],
          roles: ['SUPER_ADMIN', 'ADMIN'],
        },
      },
      {
        path: 'posts',
        component: () => import('../views/posts/index.vue'),
        meta: {
          title: '岗位管理',
          permissions: ['post:list'],
          roles: ['SUPER_ADMIN', 'ADMIN'],
        },
      },
      {
        path: 'dicts',
        component: () => import('../views/dicts/index.vue'),
        meta: {
          title: '字典管理',
          permissions: ['dict:list'],
          roles: ['SUPER_ADMIN', 'ADMIN'],
        },
      },
      {
        path: 'notices',
        component: () => import('../views/notices/index.vue'),
        meta: {
          title: '通知公告',
          permissions: ['notice:list'],
          roles: ['SUPER_ADMIN', 'ADMIN', 'OPERATOR'],
        },
      },
      {
        path: 'roles',
        component: () => import('../views/roles/index.vue'),
        meta: {
          title: '角色管理',
          permissions: ['role:list'],
          roles: ['SUPER_ADMIN', 'ADMIN'],
        },
      },
      {
        path: 'permissions',
        component: () => import('../views/permissions/index.vue'),
        meta: {
          title: '权限管理',
          permissions: ['permission:list', 'role:permission'],
          roles: ['SUPER_ADMIN', 'ADMIN'],
        },
      },
      {
        path: 'operation-logs',
        component: () => import('../views/operation-logs/index.vue'),
        meta: {
          title: '操作日志',
          permissions: ['operation-log:list'],
          roles: ['SUPER_ADMIN', 'ADMIN'],
        },
      },
      {
        path: 'settings',
        component: () => import('../views/settings/index.vue'),
        meta: {
          title: '系统设置',
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: window.location.hostname === 'editsrepresas-cell.github.io'
    ? createWebHashHistory(import.meta.env.BASE_URL)
    : createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
router.beforeEach((to) => {
  const pageTitle = to.matched
    .map((routeRecord) => routeRecord.meta.title)
    .filter(Boolean)
    .at(-1)

  document.title = pageTitle ? `${pageTitle} - 后台管理系统` : '后台管理系统'

  const token = localStorage.getItem('admin_token')

  if (to.path !== '/login' && !token) {
    return '/login'
  }

  if (to.path === '/login' && token) {
    return '/'
  }

  if (hasLoginUser()) {
    const canOpenRoute = to.matched.every((routeRecord) =>
      canAccess({
        permissions: routeRecord.meta.permissions as string[] | undefined,
        roles: routeRecord.meta.roles as string[] | undefined,
      }),
    )

    if (!canOpenRoute) {
      return '/dashboard'
    }
  }

  return true
})
