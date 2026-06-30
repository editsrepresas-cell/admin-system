<script setup lang="ts">
import { computed, onMounted, ref, type Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Bell,
  DataBoard,
  Document,
  Fold,
  Key,
  OfficeBuilding,
  Operation,
  Setting,
  User,
} from '@element-plus/icons-vue'
import { getCurrentUser, type LoginVO } from '../api/auth'
import {
  getUnreadNotices,
  markAllNoticesRead,
  markNoticeRead,
  type NoticeItem,
} from '../api/notice'
import { canAccess, type AccessRule } from '../utils/permission'

const router = useRouter()
const route = useRoute()

const collapsed = ref(false)
const unreadLoading = ref(false)
const unreadNotices = ref<NoticeItem[]>([])
const loginUser = ref<Partial<LoginVO>>({
  nickname: '未登录',
  roleCode: '',
  roleName: '',
  permissionCodes: [],
})

const activeMenu = computed(() => route.path)
const asideWidth = computed(() => (collapsed.value ? '64px' : '220px'))
const unreadCount = computed(() => unreadNotices.value.length)

interface MenuItem extends AccessRule {
  path: string
  title: string
  icon: Component
}

const allMenus: MenuItem[] = [
  {
    path: '/dashboard',
    title: '仪表盘',
    icon: DataBoard,
  },
  {
    path: '/users',
    title: '用户管理',
    icon: User,
    permissions: ['user:list'],
    roles: ['SUPER_ADMIN', 'ADMIN', 'OPERATOR'],
  },
  {
    path: '/depts',
    title: '部门管理',
    icon: OfficeBuilding,
    permissions: ['dept:list'],
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    path: '/posts',
    title: '岗位管理',
    icon: OfficeBuilding,
    permissions: ['post:list'],
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    path: '/dicts',
    title: '字典管理',
    icon: Document,
    permissions: ['dict:list'],
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    path: '/notices',
    title: '通知公告',
    icon: Bell,
    permissions: ['notice:list'],
    roles: ['SUPER_ADMIN', 'ADMIN', 'OPERATOR'],
  },
  {
    path: '/roles',
    title: '角色管理',
    icon: Key,
    permissions: ['role:list'],
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    path: '/permissions',
    title: '权限管理',
    icon: Operation,
    permissions: ['permission:list', 'role:permission'],
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    path: '/operation-logs',
    title: '操作日志',
    icon: Document,
    permissions: ['operation-log:list'],
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    path: '/settings',
    title: '系统设置',
    icon: Setting,
  },
]

const menuItems = computed(() => {
  return allMenus.filter((menu) => canAccess(menu, loginUser.value))
})

const loadCurrentUser = async () => {
  const res = await getCurrentUser()
  loginUser.value = res.data
  localStorage.setItem('admin_token', res.data.token)
  localStorage.setItem('admin_user', JSON.stringify(res.data))
}

const loadUnreadNotices = async () => {
  unreadLoading.value = true
  try {
    const res = await getUnreadNotices()
    unreadNotices.value = res.data
  } finally {
    unreadLoading.value = false
  }
}

const toggleMenu = () => {
  collapsed.value = !collapsed.value
}

const handleBellClick = () => {
  router.push('/notices')
}

const handleNoticeClick = async (notice: NoticeItem) => {
  await markNoticeRead(notice.id)
  await loadUnreadNotices()
  router.push({ path: '/notices', query: { noticeId: notice.id } })
}

const handleReadAll = async () => {
  await markAllNoticesRead()
  await loadUnreadNotices()
}

const handleLogout = async () => {
  await ElMessageBox.confirm('确认退出当前账号吗？', '退出确认', {
    type: 'warning',
    confirmButtonText: '退出',
    cancelButtonText: '取消',
  })

  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_user')
  router.push('/login')
}

onMounted(async () => {
  await loadCurrentUser()
  loadUnreadNotices()
})
</script>

<template>
  <el-container class="admin-layout">
    <el-aside class="admin-aside" :width="asideWidth">
      <div class="logo" :class="{ compact: collapsed }">
        {{ collapsed ? '管' : '后台管理系统' }}
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        class="admin-menu"
        background-color="#001529"
        text-color="#c0c4cc"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item v-for="menu in menuItems" :key="menu.path" :index="menu.path">
          <el-icon><component :is="menu.icon" /></el-icon>
          <template #title>
            <span>{{ menu.title }}</span>
          </template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-button class="collapse-button" text @click="toggleMenu">
            <el-icon><Fold /></el-icon>
          </el-button>
          <span>管理后台</span>
        </div>

        <div class="header-right">
          <el-popover
            placement="bottom-end"
            trigger="hover"
            width="340"
            popper-class="notice-popover"
            @show="loadUnreadNotices"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                <el-button class="notice-button" text @click="handleBellClick">
                  <el-icon><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>

            <div class="notice-panel" v-loading="unreadLoading">
              <div class="notice-panel-header">
                <span>未读消息</span>
                <el-button v-if="unreadCount > 0" link type="primary" @click="handleReadAll">
                  全部已读
                </el-button>
              </div>

              <div v-if="unreadCount === 0" class="notice-empty">
                暂无未读消息
              </div>

              <button
                v-for="notice in unreadNotices"
                :key="notice.id"
                class="notice-item"
                type="button"
                @click="handleNoticeClick(notice)"
              >
                <span class="notice-title">{{ notice.title }}</span>
                <span class="notice-time">{{ notice.publishTime || notice.createTime || '-' }}</span>
              </button>
            </div>
          </el-popover>

          <div class="user-meta">
            <span class="nickname">{{ loginUser.nickname }}</span>
            <span class="role-name">{{ loginUser.roleName }}</span>
          </div>

          <el-button link type="primary" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  width: 100vw;
  height: 100vh;
  min-height: 100vh;
  overflow: hidden;
}

.admin-layout > .el-container {
  min-width: 0;
  flex: 1;
  height: 100vh;
  overflow: hidden;
}

.admin-aside {
  background: #001529;
  transition: width 0.2s ease;
}

.logo {
  height: 56px;
  line-height: 56px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
  border-bottom: 1px solid #10263f;
  white-space: nowrap;
  overflow: hidden;
}

.logo.compact {
  font-size: 20px;
}

.admin-menu {
  border-right: none;
}

.admin-menu:not(.el-menu--collapse) {
  width: 220px;
}

.admin-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 20px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.collapse-button,
.notice-button {
  width: 32px;
  height: 32px;
  padding: 0;
  color: #0f172a;
}

.collapse-button:hover,
.notice-button:hover {
  background: #f1f5f9;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.nickname {
  color: #1f2937;
  font-size: 14px;
}

.role-name {
  color: #8a8f99;
  font-size: 12px;
}

.admin-main {
  background: #f5f7fb;
  height: calc(100vh - 56px);
  min-height: 0;
  padding: 20px;
  overflow-x: hidden;
  overflow-y: auto;
}

.notice-panel {
  min-height: 82px;
}

.notice-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #0f172a;
  font-weight: 700;
}

.notice-empty {
  padding: 22px 0;
  color: #94a3b8;
  text-align: center;
}

.notice-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 8px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.notice-item:hover {
  background: #f8fafc;
}

.notice-title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  color: #94a3b8;
  font-size: 12px;
}
</style>
